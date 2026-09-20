package io.gifsync.septemcraft.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks what a circuit is once it has been assembled, and in particular what a conductor resisting
 * nothing does to it. Two blocks bolted together is the most common thing in a built grid and the one
 * a solver working in conductance cannot take at face value, so what a caller sees of that is
 * worth pinning: nothing at all.
 */
class CircuitAssemblyTest
{
	/**
	 * How far two readings that should be one number may stray before they are two. A joint
	 * resisting nothing is contracted away before anything is solved, so both its ends come out of
	 * the same arithmetic, and a circuit that was not solved is filled with zeros outright.
	 */
	private static final double EXACT = 1.0e-12;

	/**
	 * How far two figures that each came out of a solve of their own may stray and still be the
	 * same answer. Looser than {@link #EXACT} because nothing promises the two circuits were solved
	 * by the same arithmetic, only that they agree.
	 */
	private static final double CLOSE = 1.0e-9;

	/** A solver deliberately sized for far less than the mod's own, so a test can outgrow it cheaply. */
	private static final int SMALL = 16;

	/** Every node put into a circuit is in the circuit, regardless of whether anything came to join it. */
	@Test
	void aCircuitHoldsEveryNodeItWasBuiltWith()
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId first = builder.node();
		NodeId second = builder.node();
		NodeId lonely = builder.node();
		builder.conductor(first, second, new Ohms(1.0));

		Circuit circuit = builder.build();

		assertEquals(3, circuit.nodes().size());
		assertTrue(circuit.nodes().containsAll(java.util.List.of(first, second, lonely)));
	}

	/**
	 * A joint between two blocks resists nothing, so putting one in the middle of a run leaves every
	 * reading elsewhere exactly where it was. A joint that does resist something does not.
	 */
	@Test
	void aBoltedJointChangesNothingAboutTheAnswer()
	{
		CircuitSolver solver = new CircuitSolver();

		Fixtures.Divider plain = Fixtures.divider();
		Fixtures.JointedDivider bolted = Fixtures.jointedDivider(Ohms.ZERO);
		Fixtures.JointedDivider welded = Fixtures.jointedDivider(new Ohms(2.0));

		double plainly = solver.solve(plain.circuit()).voltageAt(plain.tap()).value();
		double jointed = solver.solve(bolted.circuit()).voltageAt(bolted.tap()).value();
		double resisted = solver.solve(welded.circuit()).voltageAt(welded.tap()).value();

		assertEquals(plainly, jointed, CLOSE, "a joint resisting nothing moved the answer");
		assertNotEquals(plainly, resisted, "a joint resisting two ohms did not move the answer");
	}

	/** Two points bolted together are one point, so both of them read the same potential. */
	@Test
	void bothEndsOfABoltedJointReadTheSamePotential()
	{
		Fixtures.JointedDivider bolted = Fixtures.jointedDivider(Ohms.ZERO);

		Solution solution = new CircuitSolver().solve(bolted.circuit());

		assertEquals(solution.voltageAt(bolted.middle()).value(), solution.voltageAt(bolted.tap()).value(), EXACT);
		assertEquals(0.0, solution.across(bolted.middle(), bolted.tap()).value(), EXACT);
	}

	/** What runs through a bolted joint is whatever the run it sits in is carrying. */
	@Test
	void aBoltedJointInARunCarriesWhatTheRunCarries()
	{
		Fixtures.JointedDivider bolted = Fixtures.jointedDivider(Ohms.ZERO);

		Solution solution = new CircuitSolver().solve(bolted.circuit());

		assertEquals(1.0, solution.through(bolted.joint()).value(), CLOSE);
		assertEquals(solution.through(bolted.load()).value(), solution.through(bolted.joint()).value(), CLOSE);
	}

	/**
	 * A bolted loop hanging off a circuit has nothing to carry, and any current going round and
	 * round it would satisfy the circuit equally. None of them is the answer, so it carries none.
	 */
	@Test
	void aBoltedLoopWithNothingToCarryCarriesNothing()
	{
		Fixtures.Divider divider = Fixtures.divider();
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId tap = builder.node();
		NodeId corner = builder.node();
		NodeId farCorner = builder.node();

		builder.source(reference, live, new Volts(12.0), Ohms.ZERO);
		builder.conductor(live, tap, new Ohms(8.0));
		builder.load(tap, reference, LoadClass.CONSTANT_RESISTANCE, new Watts(36.0), new Volts(12.0), Volts.ZERO);
		Conductor first = builder.conductor(tap, corner, Ohms.ZERO);
		Conductor second = builder.conductor(corner, farCorner, Ohms.ZERO);
		Conductor third = builder.conductor(farCorner, tap, Ohms.ZERO);

		Solution solution = new CircuitSolver().solve(builder.build());

		assertEquals(0.0, solution.through(first).value(), CLOSE);
		assertEquals(0.0, solution.through(second).value(), CLOSE);
		assertEquals(0.0, solution.through(third).value(), CLOSE);
		assertEquals(new CircuitSolver().solve(divider.circuit()).voltageAt(divider.tap()).value(),
			solution.voltageAt(tap).value(), CLOSE);
	}

	/**
	 * A grid a player has built is mostly blocks bolted to blocks, and those collapse into one point
	 * before anything is solved. The solver's limit is on the points a circuit really has, not on
	 * the blocks it was assembled from, so a run far longer than that limit still solves.
	 */
	@Test
	void aLongRunOfBoltedBlocksIsNotTooLargeToSolve()
	{
		CircuitSolver solver = new CircuitSolver(SMALL);

		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		builder.source(reference, live, Fixtures.NOMINAL, Ohms.ZERO);

		NodeId previous = live;
		for (int block = 0; block < solver.maxNodes() * 2; block++)
		{
			NodeId next = builder.node();
			builder.conductor(previous, next, Ohms.ZERO);
			previous = next;
		}

		builder.load(previous, reference, LoadClass.CONSTANT_RESISTANCE, Fixtures.RATING, Fixtures.NOMINAL,
			Volts.ZERO);

		Solution solution = solver.solve(builder.build());

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(Fixtures.NOMINAL.value(), solution.voltageAt(previous).value(), CLOSE);
	}

	/**
	 * A circuit with more points than the solver is sized for is refused as a whole rather than
	 * solved slowly, and reads as nothing so that whatever asked still has figures to show.
	 */
	@Test
	void aCircuitLargerThanTheSolverIsSizedForReadsAsTooLarge()
	{
		CircuitSolver solver = new CircuitSolver(SMALL);

		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId previous = reference;
		for (int step = 0; step < solver.maxNodes() + 2; step++)
		{
			NodeId next = builder.node();
			builder.conductor(previous, next, new Ohms(1.0));
			previous = next;
		}

		builder.source(reference, previous, Fixtures.NOMINAL, Ohms.ZERO);

		Solution solution = solver.solve(builder.build());

		assertEquals(SolutionStatus.TOO_LARGE, solution.status());
		assertEquals(0.0, solution.voltageAt(previous).value(), EXACT);
	}

	/** A run of conductor joined to nothing that drives it sits at nothing, and does not stop the rest solving. */
	@Test
	void anIslandWithNothingDrivingItReadsAsDead()
	{
		Fixtures.Island island = Fixtures.island();

		Solution solution = new CircuitSolver().solve(island.circuit());

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(0.0, solution.voltageAt(island.adrift()).value(), EXACT);
		assertEquals(0.0, solution.voltageAt(island.alsoAdrift()).value(), EXACT);
		assertEquals(0.0, solution.through(island.stranded()).value(), EXACT);
		CircuitAssertions.assertConserves(island.circuit(), solution);
	}

	/** A circuit with nothing driving it at all is a legal circuit that reads as nothing. */
	@Test
	void aCircuitWithNothingDrivingItReadsAsDeEnergised()
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId far = builder.node();
		Conductor conductor = builder.conductor(reference, far, new Ohms(4.0));

		Solution solution = new CircuitSolver().solve(builder.build());

		assertEquals(SolutionStatus.DE_ENERGISED, solution.status());
		assertEquals(0.0, solution.voltageAt(far).value(), EXACT);
		assertEquals(0.0, solution.through(conductor).value(), EXACT);
	}
}
