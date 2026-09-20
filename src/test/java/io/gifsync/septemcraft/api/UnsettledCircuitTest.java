package io.gifsync.septemcraft.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks what a circuit that cannot settle reads as. A device holding its power has an answer at all
 * only while its line resists less than the machine's own potential squared over four times that
 * power. Past that there is nothing for a solver to find: the device collapses the line, gives up,
 * recovers, and collapses it again, and no number of passes lands on an answer. What is held is the
 * last reading taken, and it still accounts for itself.
 */
class UnsettledCircuitTest
{
	/** The potential the machine here drives, which is this test's figure and no rule. */
	private static final Volts NOMINAL = new Volts(128.0);

	/** The power the device here holds, which is this test's figure and no rule. */
	private static final Watts RATING = new Watts(1024.0);

	/** Where the device here gives up, which is half what it is rated for. */
	private static final Volts FLOOR = new Volts(NOMINAL.value() / 2.0);

	/**
	 * How much of the resistance a line could carry and still be answerable this line carries. Past
	 * all of it the device satisfies the circuit at no potential whatever, and every pass moves the
	 * working figure by volts rather than closing on one.
	 */
	private static final double LINE_PAST_ITS_CEILING = 1.25;

	/**
	 * A machine just clear of the device's floor, which is the share of it that leaves the working
	 * potentials below that floor for two passes running.
	 */
	private static final double JUST_CLEAR_OF_THE_FLOOR = 1.1;

	/** A line too resistive for what is on the end of it never settles, however many passes it gets. */
	@Test
	void aDeviceTooLargeForItsLineNeverSettles()
	{
		Circuit circuit = feedDrivenAt(NOMINAL);

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(SolutionStatus.UNSETTLED, solution.status());
	}

	/**
	 * The readings held are a real electrical state and not a half-finished one: current balances at
	 * every node and every watt is accounted for. Only their being final is missing, which is why they
	 * are held rather than thrown away.
	 */
	@Test
	void theReadingsOfACircuitThatNeverSettledStillAddUp()
	{
		Circuit circuit = feedDrivenAt(NOMINAL);

		Solution solution = new CircuitSolver().solve(circuit);

		CircuitAssertions.assertReadable(circuit, solution);
		CircuitAssertions.assertConserves(circuit, solution);
	}

	/**
	 * Two passes reading alike is not an answer. A machine sitting just clear of the device's floor
	 * leaves the working potentials below that floor for two passes running, so the line reads open
	 * on both and the device reads as having given up - while the potential reported across its own
	 * terminals is one it runs at. No circuit is in that state, and a solve has settled on nothing by
	 * arriving at it.
	 */
	@Test
	void aDeviceIsNeverAnsweredGivenUpAtAPotentialItRunsAt()
	{
		Circuit circuit = feedDrivenAt(shareOfTheFloor(JUST_CLEAR_OF_THE_FLOOR));

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(SolutionStatus.UNSETTLED, solution.status());
	}

	/**
	 * Nothing the solver answers reads as a state its devices are not in, whatever the machine is
	 * driving at. Two passes reading alike would otherwise be taken for an answer wherever the
	 * working potentials happen to straddle a device's floor.
	 */
	@ParameterizedTest
	@ValueSource(doubles = {0.5, 0.75, 0.99, 1.0, 1.05, 1.1, 1.2, 1.3, 1.5, 2.0})
	void aCircuitTheSolverAnswersReadsAsAStateItsDevicesAreIn(double share)
	{
		Circuit circuit = feedDrivenAt(shareOfTheFloor(share));

		Solution solution = new CircuitSolver().solve(circuit);

		CircuitAssertions.assertAnswers(circuit, solution);
	}

	/**
	 * A machine that leaves the device below its own floor is answered, and one that lifts it over is
	 * not, this line resisting more than the device leaves room for at every potential named. The
	 * floor itself is what separates the two outcomes, rather than a figure either of them is tuned
	 * to.
	 */
	@ParameterizedTest
	@ValueSource(doubles = {0.5, 0.75, 0.99, 1.0, 1.05, 1.1, 1.2, 1.3, 1.5, 2.0})
	void aFeedIsAnsweredOnlyWhileItLeavesItsDeviceBelowItsFloor(double share)
	{
		Circuit circuit = feedDrivenAt(shareOfTheFloor(share));

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(share < 1.0, solution.status() == SolutionStatus.SOLVED,
			"a machine at " + share + " of the device's floor read " + solution.status());
	}

	/** The potential a share of the device's floor comes to. */
	private static Volts shareOfTheFloor(double share)
	{
		return new Volts(share * FLOOR.value());
	}

	/**
	 * A machine driving the potential named into a device that holds its power, down a line out and
	 * as many blocks back. The line is sized past what that device leaves room for at its own rated
	 * potential, which is the most any machine here drives, so it is past that ceiling at every
	 * potential here and what changes between them is only whether the device starts at all.
	 */
	private static Circuit feedDrivenAt(Volts driving)
	{
		Ohms line = new Ohms(LINE_PAST_ITS_CEILING * Fixtures.answerableCeiling(NOMINAL, RATING).value());
		assertTrue(line.value() > Fixtures.answerableCeiling(driving, RATING).value(),
			"a line of " + line + " leaves the device room to run at " + driving + ", so it settles");

		Ohms arm = new Ohms(line.value() / 2.0);

		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId farLive = builder.node();
		NodeId farReturn = builder.node();

		builder.source(reference, live, driving, Ohms.ZERO);
		builder.conductor(live, farLive, arm);
		builder.conductor(farReturn, reference, arm);
		builder.load(farLive, farReturn, LoadClass.CONSTANT_POWER, RATING, NOMINAL, FLOOR);

		return builder.build();
	}
}
