package io.gifsync.septemcraft.api;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the solver against circuits whose answers can be worked out by hand from the figures the
 * fixture itself names, and against the two things that make this harder than one pass of
 * arithmetic: a device that answers its own supply, and a path that resists nothing.
 */
class SolverTest
{
	/** How close a solved figure counts as matching one worked out by hand. */
	private static final double CLOSE = 1.0e-6;

	/** Eight ohms above four ohms leaves a third of twelve volts at the tap. */
	@Test
	void aResistiveDividerSplitsItsPotentialByItsArms()
	{
		Fixtures.Divider divider = Fixtures.divider();

		Solution solution = new CircuitSolver().solve(divider.circuit());

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(12.0, solution.voltageAt(divider.live()).value(), CLOSE);
		assertEquals(4.0, solution.voltageAt(divider.tap()).value(), CLOSE);
		assertEquals(1.0, solution.through(divider.arm()).value(), CLOSE);
		assertEquals(1.0, solution.through(divider.source()).value(), CLOSE);
		assertEquals(4.0, solution.drawnBy(divider.load()).value(), CLOSE);
	}

	/**
	 * Two conductors side by side carry current in proportion to how well each conducts, and
	 * together they carry more than either. A solver that writes a branch into its matrix where it
	 * should add one gets the single arm right and this wrong.
	 */
	@Test
	void aParallelPairSharesItsCurrentByItsConductance()
	{
		Fixtures.ParallelPair pair = Fixtures.parallelPair();

		Solution solution = new CircuitSolver().solve(pair.circuit());

		assertEquals(8.0, solution.voltageAt(pair.tap()).value(), CLOSE);
		assertEquals(4.0 / 3.0, solution.through(pair.wide()).value(), CLOSE);
		assertEquals(2.0 / 3.0, solution.through(pair.narrow()).value(), CLOSE);
		assertEquals(2.0, solution.through(pair.source()).value(), CLOSE);
	}

	/**
	 * A bridge is the smallest circuit that no folding of series and parallel resistances answers,
	 * so getting it right means the solver is solving rather than reducing. Its middle conductor
	 * also runs backwards against the way it was put in, which is where a sign convention shows.
	 */
	@Test
	void anUnbalancedBridgeCarriesCurrentAcrossItself()
	{
		Fixtures.Bridge bridge = Fixtures.bridge();

		Solution solution = new CircuitSolver().solve(bridge.circuit());

		assertEquals(16.0 / 3.0, solution.voltageAt(bridge.leftCorner()).value(), CLOSE);
		assertEquals(20.0 / 3.0, solution.voltageAt(bridge.rightCorner()).value(), CLOSE);
		assertEquals(-2.0 / 3.0, solution.through(bridge.across()).value(), CLOSE);
		assertEquals(26.0 / 9.0, solution.through(bridge.source()).value(), CLOSE);
	}

	/**
	 * A loop feeds the point opposite down both of its sides at once, so the tap sees the two halves
	 * in parallel: two sides of two edges each, which is one edge between them.
	 */
	@Test
	void aLoopFeedsItsFarTapDownBothSidesAtOnce()
	{
		Fixtures.Ring ring = Fixtures.ring(true);

		Solution solution = new CircuitSolver().solve(ring.circuit());

		assertEquals(ring.edge().value(), solution.across(ring.feed(), ring.tap()).value(), CLOSE);
	}

	/** A loop broken at one point is a run, and still feeds its tap down the side that is left. */
	@Test
	void aLoopCutOnOneSideStillFeedsItsFarTap()
	{
		Fixtures.Ring ring = Fixtures.ring(false);

		Solution solution = new CircuitSolver().solve(ring.circuit());

		assertEquals(2.0 * ring.edge().value(), solution.across(ring.feed(), ring.tap()).value(), CLOSE);
	}

	/**
	 * A device holding its power draws harder as its supply sags, and sagging further is what makes
	 * it draw harder still. Two potentials satisfy that circuit and only the upper one is stable:
	 * the lower is the collapse the grid would have to already be in to sit there. The fixture
	 * works both out from its own figures, so this names the answer rather than a half of something.
	 */
	@Test
	void aFeedHoldingItsPowerSettlesOnTheStableAnswer()
	{
		Fixtures.Feed feed = Fixtures.singleFeed();

		Solution solution = new CircuitSolver().solve(feed.circuit());

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(feed.stable().value(), solution.across(feed.farLive(), feed.farReturn()).value(), CLOSE);
		assertEquals(Fixtures.RATING.value(), solution.drawnBy(feed.load()).value(), CLOSE);
		CircuitAssertions.assertConserves(feed.circuit(), solution);
	}

	/**
	 * What the last tick left behind must not decide what this tick reads. A grid seeded anywhere at
	 * all - dead, mid-collapse, or above the machine that feeds it - reaches the one answer the line
	 * really settles at, so a sag that has already happened recovers rather than latching.
	 */
	@ParameterizedTest
	@ValueSource(doubles = {0.0, 0.05, 0.25, 0.4, 0.5, 0.6, 0.75, 1.0, 2.0})
	void aFeedHoldingItsPowerClimbsBackToTheStableAnswer(double shareOfTheMachinesPotential)
	{
		Fixtures.Feed feed = Fixtures.singleFeed();
		CircuitSolver solver = new CircuitSolver();

		Map<NodeId, Volts> seed = new HashMap<>();
		seed.put(feed.reference(), Volts.ZERO);
		seed.put(feed.live(), Fixtures.NOMINAL);
		seed.put(feed.farLive(), new Volts(shareOfTheMachinesPotential * Fixtures.NOMINAL.value()));
		seed.put(feed.farReturn(), Volts.ZERO);

		Solution fromSeed = solver.solveFrom(feed.circuit(), seed);

		assertEquals(SolutionStatus.SOLVED, fromSeed.status());
		assertEquals(feed.stable().value(), fromSeed.across(feed.farLive(), feed.farReturn()).value(), CLOSE);
	}

	/**
	 * The collapsed answer is the one a line sags into and does not climb out of, and it is only
	 * reachable by a device that would still be running down there. A device that gives up above it
	 * cannot hold the circuit in it, so the solver has one answer to find rather than the right one
	 * of two.
	 */
	@Test
	void theCollapsedAnswerIsBelowWhereTheDeviceGivesUp()
	{
		Fixtures.Feed feed = Fixtures.singleFeed();

		assertTrue(feed.collapsed().value() < feed.load().minimumVoltage().value(),
			"the device would still be running at the collapsed answer");
		assertTrue(feed.stable().value() > feed.load().minimumVoltage().value(),
			"the device gives up before the answer the line settles at");
	}

	/**
	 * A transformer turns the potential across its primary into the ratio of it across its
	 * secondary, and takes its share of the power on the way exactly once. Taking it twice, or on
	 * the wrong side, still balances at a glance and is wrong by that share.
	 */
	@Test
	void aTransformerDeliversWhatArrivesLessExactlyOneDeduction()
	{
		LossFraction loss = new LossFraction(0.05);
		Fixtures.Coupled coupled = Fixtures.coupled(new TurnsRatio(2.0), loss);

		Solution solution = new CircuitSolver().solve(coupled.circuit());

		double arriving = solution.arrivingAt(coupled.transformer()).value();
		double leaving = solution.leaving(coupled.transformer()).value();

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(2.0 * Fixtures.NOMINAL.value(),
			solution.across(coupled.secondaryLive(), coupled.secondaryReturn()).value(), CLOSE);
		assertEquals(arriving * (1.0 - loss.fraction()), leaving, CLOSE);
		assertEquals(leaving, solution.drawnBy(coupled.load()).value(), CLOSE);
	}

	/**
	 * Two machines across one bus cannot each hold it at their own potential, so it settles between
	 * them: the faster drives the bus and the slower is driven by it. A machine being driven is not
	 * an error state, it is a motor, and it reads as delivering less than nothing.
	 */
	@Test
	void twoMachinesOnOneBusSettleBetweenThemAndTheSlowOneIsDriven()
	{
		Fixtures.TwoMachines machines = Fixtures.twoMachines();

		Solution solution = new CircuitSolver().solve(machines.circuit());

		double bus = solution.voltageAt(machines.live()).value();

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertTrue(bus > machines.slow().electromotiveForce().value(), "the bus sat at or below the slow machine");
		assertTrue(bus < machines.fast().electromotiveForce().value(), "the bus sat at or above the fast machine");

		assertTrue(solution.through(machines.fast()).value() > 0.0, "the fast machine is not driving");
		assertTrue(solution.through(machines.slow()).value() < 0.0, "the slow machine is not being driven");
		assertTrue(solution.deliveredBy(machines.slow()).value() < 0.0, "the driven machine still reads as delivering");

		assertEquals(solution.through(machines.load()).value(),
			solution.through(machines.fast()).value() + solution.through(machines.slow()).value(), CLOSE);
	}

	/**
	 * Two machines that lose nothing in their own windings and disagree about the potential cannot
	 * both be obeyed, and no current is large enough to settle it. That has no answer rather than a
	 * huge one, and reads as nothing so that whatever asked still has figures to show.
	 */
	@Test
	void twoIdealMachinesThatContradictEachOtherHaveNoAnswer()
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		builder.source(reference, live, new Volts(128.0), Ohms.ZERO);
		builder.source(reference, live, new Volts(120.0), Ohms.ZERO);

		Solution solution = new CircuitSolver().solve(builder.build());

		assertEquals(SolutionStatus.SINGULAR, solution.status());
		assertEquals(0.0, solution.voltageAt(live).value(), CLOSE);
	}

	/**
	 * A machine that loses nothing in its own windings, with a bolted path from one terminal back to
	 * the other, is asked for a current no figure answers. That is a fault to be shown as one, not
	 * an infinity to be handed on to whatever is reading it.
	 */
	@Test
	void aMachineBoltedBackToItselfIsAFault()
	{
		Fixtures.ShortCircuit shorted = Fixtures.shortedThrough(Ohms.ZERO);

		Solution solution = new CircuitSolver().solve(shorted.circuit());

		assertEquals(SolutionStatus.SHORTED, solution.status());
		CircuitAssertions.assertReadable(shorted.circuit(), solution);
	}

	/**
	 * The same short behind a machine's own windings is not a fault at all. The windings hold the
	 * current to what they allow, which is a large figure and a real one, and the machine delivers
	 * none of it anywhere because its terminals are held together.
	 */
	@Test
	void aMachineShortedBehindItsOwnWindingsIsLargeButNotAFault()
	{
		Fixtures.ShortCircuit shorted = Fixtures.shortedThrough(Fixtures.WINDING);

		Solution solution = new CircuitSolver().solve(shorted.circuit());

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(Fixtures.NOMINAL.over(Fixtures.WINDING).value(),
			solution.through(shorted.source()).value(), CLOSE);
		assertEquals(0.0, solution.deliveredBy(shorted.source()).value(), CLOSE);
	}
}
