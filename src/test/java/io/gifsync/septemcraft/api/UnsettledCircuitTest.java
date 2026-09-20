package io.gifsync.septemcraft.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

	/**
	 * How much of the resistance a line could carry and still be answerable this line carries. Past
	 * all of it the device satisfies the circuit at no potential whatever, and every pass moves the
	 * working figure by volts rather than closing on one.
	 */
	private static final double LINE_PAST_ITS_CEILING = 1.25;

	/** A line too resistive for what is on the end of it never settles, however many passes it gets. */
	@Test
	void aDeviceTooLargeForItsLineNeverSettles()
	{
		Circuit circuit = feedPastItsCeiling();

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
		Circuit circuit = feedPastItsCeiling();

		Solution solution = new CircuitSolver().solve(circuit);

		CircuitAssertions.assertReadable(circuit, solution);
		CircuitAssertions.assertConserves(circuit, solution);
	}

	/**
	 * A machine feeding a device that holds its power, down a line out and as many blocks back that
	 * resists more than the device leaves room for. The resistance is worked back from that ceiling
	 * rather than written down, so the line is past it by construction.
	 */
	private static Circuit feedPastItsCeiling()
	{
		double ceiling = NOMINAL.value() * NOMINAL.value() / (4.0 * RATING.value());
		Ohms arm = new Ohms(LINE_PAST_ITS_CEILING * ceiling / 2.0);

		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId farLive = builder.node();
		NodeId farReturn = builder.node();

		builder.source(reference, live, NOMINAL, Ohms.ZERO);
		builder.conductor(live, farLive, arm);
		builder.conductor(farReturn, reference, arm);
		builder.load(farLive, farReturn, LoadClass.CONSTANT_POWER, RATING, NOMINAL, new Volts(NOMINAL.value() / 2.0));

		return builder.build();
	}
}
