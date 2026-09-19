package io.gifsync.septemcraft.electrification.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the solver against circuits whose answers are known by hand, and against the transmission
 * fixtures the electrical model is written around.
 */
class CircuitSolverTest
{
	/** How close a solved figure counts as matching one worked out by hand. */
	private static final double CLOSE = 1.0e-6;

	/** The reference node of every fixture below, and the return conductor's own node where there is one. */
	private static final NodeId RETURN = new NodeId(0);

	private static final NodeId LIVE = new NodeId(1);

	private static final NodeId FAR_LIVE = new NodeId(2);

	private static final NodeId FAR_RETURN = new NodeId(3);

	@Test
	void aResistiveDividerSplitsItsVoltageByItsArms()
	{
		List<NodeId> nodes = nodes(3);
		Source source = new Source(LIVE, RETURN, new Volts(12.0), Ohms.ZERO);
		Conductor upper = new Conductor(LIVE, FAR_LIVE, new Ohms(8.0));
		Load lower = new Load(FAR_LIVE, RETURN, LoadClass.CONSTANT_RESISTANCE, new Watts(36.0), new Volts(12.0));
		Circuit circuit = new Circuit(nodes, List.of(upper), List.of(source), List.of(lower), List.of());

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(12.0, solution.voltageAt(LIVE).value(), CLOSE);
		assertEquals(4.0, solution.voltageAt(FAR_LIVE).value(), CLOSE);
		assertEquals(1.0, solution.through(upper).value(), CLOSE);
		assertEquals(1.0, solution.through(source).value(), CLOSE);
	}

	@Test
	void aParallelPairSharesItsCurrentByItsConductances()
	{
		List<NodeId> nodes = nodes(3);
		Source source = new Source(LIVE, RETURN, new Volts(12.0), Ohms.ZERO);
		Conductor wide = new Conductor(LIVE, FAR_LIVE, new Ohms(3.0));
		Conductor narrow = new Conductor(LIVE, FAR_LIVE, new Ohms(6.0));
		Load load = new Load(FAR_LIVE, RETURN, LoadClass.CONSTANT_RESISTANCE, new Watts(36.0), new Volts(12.0));
		Circuit circuit = new Circuit(nodes, List.of(wide, narrow), List.of(source), List.of(load), List.of());

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(8.0, solution.voltageAt(FAR_LIVE).value(), CLOSE);
		assertEquals(4.0 / 3.0, solution.through(wide).value(), CLOSE);
		assertEquals(2.0 / 3.0, solution.through(narrow).value(), CLOSE);
		assertEquals(2.0, solution.through(source).value(), CLOSE);
	}

	@Test
	void anUnbalancedBridgeCarriesCurrentAcrossItself()
	{
		List<NodeId> nodes = nodes(4);
		Source source = new Source(LIVE, RETURN, new Volts(12.0), Ohms.ZERO);
		Conductor bridge = new Conductor(FAR_LIVE, FAR_RETURN, new Ohms(2.0));
		Circuit circuit = new Circuit(nodes,
			List.of(new Conductor(LIVE, FAR_LIVE, new Ohms(6.0)), new Conductor(LIVE, FAR_RETURN, new Ohms(3.0)),
				new Conductor(FAR_LIVE, RETURN, new Ohms(3.0)), new Conductor(FAR_RETURN, RETURN, new Ohms(6.0)),
				bridge),
			List.of(source), List.of(), List.of());

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(16.0 / 3.0, solution.voltageAt(FAR_LIVE).value(), CLOSE);
		assertEquals(20.0 / 3.0, solution.voltageAt(FAR_RETURN).value(), CLOSE);
		assertEquals(-2.0 / 3.0, solution.through(bridge).value(), CLOSE);
		assertEquals(26.0 / 9.0, solution.through(source).value(), CLOSE);
	}

	@Test
	void aRingBusbarFeedsItsFarTapDownBothSidesAtOnce()
	{
		Solution solution = new CircuitSolver().solve(ring(true));

		// Two eighteen-block paths of busbar in parallel: 0.036 ohms each, 0.018 ohms together.
		assertEquals(0.018, 1.0 - solution.across(ringNode(18), RETURN).value(), CLOSE);
	}

	@Test
	void aRingBusbarCutOnOneSideStillFeedsItsFarTap()
	{
		Solution solution = new CircuitSolver().solve(ring(false));

		// One eighteen-block path left, the rest of the ring hanging off the tap carrying nothing.
		assertEquals(0.036, 1.0 - solution.across(ringNode(18), RETURN).value(), CLOSE);
	}

	@Test
	void aSingleFeedSagsToTheAnswerItsLineAllows()
	{
		Circuit circuit = singleFeed();
		Solution solution = new CircuitSolver().solve(circuit);

		Load load = circuit.loads().get(0);
		Source source = circuit.sources().get(0);

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(32.0 / 3.0, solution.through(load).value(), CLOSE);
		assertEquals(96.0, solution.across(FAR_LIVE, FAR_RETURN).value(), CLOSE);
		assertEquals(1024.0 / 3.0, lineLoss(circuit, solution).value(), CLOSE);
		assertEquals(4096.0 / 3.0, solution.deliveredBy(source).value(), CLOSE);
	}

	@Test
	void aSingleFeedSeededNearItsUnstableAnswerStillSettlesOnTheStableOne()
	{
		Circuit circuit = singleFeed();
		Map<NodeId, Volts> seed = new HashMap<>();
		seed.put(RETURN, Volts.ZERO);
		seed.put(LIVE, new Volts(128.0));
		seed.put(FAR_LIVE, new Volts(33.0));
		seed.put(FAR_RETURN, Volts.ZERO);

		Solution solution = new CircuitSolver().solveFrom(circuit, seed);

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(96.0, solution.across(FAR_LIVE, FAR_RETURN).value(), CLOSE);
	}

	@Test
	void theSameFeedSteppedUpBurnsAlmostNoneOfIt()
	{
		Circuit circuit = steppedFeed();
		Solution solution = new CircuitSolver().solve(circuit);

		Conductor out = circuit.conductors().get(0);

		// The line now solves `(512 - 3I) x I / 4 = 1024`, the same delivery behind four times the volts.
		double current = (512.0 - Math.sqrt(249856.0)) / 6.0;

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(current, solution.through(out).value(), CLOSE);
		assertEquals(2.02, solution.through(out).value(), 1.0e-2);
		assertEquals(506.0, solution.across(new NodeId(4), new NodeId(5)).value(), 1.0e-1);
		assertEquals(3.0 * current * current, lineLoss(circuit, solution).value(), CLOSE);
		assertEquals(12.3, lineLoss(circuit, solution).value(), 1.1e-2);
	}

	@Test
	void aTransformerDeliversWhatArrivesLessExactlyOneDeduction()
	{
		List<NodeId> nodes = nodes(4);
		Transformer transformer = new Transformer(LIVE, RETURN, FAR_LIVE, FAR_RETURN, new TurnsRatio(2.0),
			new LossFraction(0.05));
		Circuit circuit = new Circuit(nodes, List.of(),
			List.of(new Source(LIVE, RETURN, new Volts(128.0), Ohms.ZERO)),
			List.of(new Load(FAR_LIVE, FAR_RETURN, LoadClass.CONSTANT_RESISTANCE, new Watts(1024.0),
				new Volts(256.0))),
			List.of(transformer));

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(256.0, solution.across(FAR_LIVE, FAR_RETURN).value(), CLOSE);
		assertEquals(1024.0, solution.leaving(transformer).value(), CLOSE);
		assertEquals(1024.0 / 0.95, solution.arrivingAt(transformer).value(), CLOSE);
		assertEquals(solution.arrivingAt(transformer).lessLoss(transformer.loss()).value(),
			solution.leaving(transformer).value(), CLOSE);
	}

	@Test
	void twoMachinesOnOneBusSettleOnOneVoltageAndTheSlowOneIsDriven()
	{
		List<NodeId> nodes = nodes(2);
		Source fast = new Source(LIVE, RETURN, new Volts(128.0), ElectricalConstants.WINDING_RESISTANCE);
		Source slow = new Source(LIVE, RETURN, new Volts(120.0), ElectricalConstants.WINDING_RESISTANCE);
		Circuit circuit = new Circuit(nodes, List.of(), List.of(fast, slow),
			List.of(new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, new Watts(1024.0), new Volts(128.0))),
			List.of());

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(121.90, solution.voltageAt(LIVE).value(), 1.0e-2);
		assertEquals(12.20, solution.through(fast).value(), 1.0e-2);
		assertEquals(-3.80, solution.through(slow).value(), 1.0e-2);
		assertTrue(solution.deliveredBy(slow).value() < 0.0, "the slow machine is being driven, not driving");
		assertEquals(-463.0, solution.deliveredBy(slow).value(), 1.0);
	}

	@Test
	void aCircuitThatDoesNotDetermineItsOwnAnswerReadsAsDead()
	{
		List<NodeId> nodes = nodes(2);
		Circuit circuit = new Circuit(nodes, List.of(),
			List.of(new Source(LIVE, RETURN, new Volts(128.0), Ohms.ZERO),
				new Source(LIVE, RETURN, new Volts(120.0), Ohms.ZERO)),
			List.of(), List.of());

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(SolutionStatus.SINGULAR, solution.status());
		assertEquals(0.0, solution.voltageAt(LIVE).value(), CLOSE);
	}

	/** The first so many nodes, which every fixture here numbers from its reference up. */
	private static List<NodeId> nodes(int count)
	{
		List<NodeId> nodes = new ArrayList<>();
		for (int index = 0; index < count; index++)
		{
			nodes.add(new NodeId(index));
		}

		return nodes;
	}

	/** The power both conductors of a line are burning between them. */
	private static Watts lineLoss(Circuit circuit, Solution solution)
	{
		Watts total = Watts.ZERO;
		for (Conductor conductor : circuit.conductors())
		{
			total = total.plus(solution.lostIn(conductor));
		}

		return total;
	}

	/**
	 * A 128 V machine feeding a 1,024 W constant-power load down 300 blocks of catenary wire, 150 out
	 * and 150 back, which is three ohms of line between the two conductors.
	 */
	private static Circuit singleFeed()
	{
		Ohms arm = ConductorForm.CATENARY_WIRE.resistanceOver(new Blocks(150));
		return new Circuit(nodes(4),
			List.of(new Conductor(LIVE, FAR_LIVE, arm), new Conductor(FAR_RETURN, RETURN, arm)),
			List.of(new Source(LIVE, RETURN, new Volts(128.0), Ohms.ZERO)),
			List.of(new Load(FAR_LIVE, FAR_RETURN, LoadClass.CONSTANT_POWER, new Watts(1024.0), new Volts(128.0))),
			List.of());
	}

	/** The same feed and the same load, behind a matched pair of transformers running the line at 512 V. */
	private static Circuit steppedFeed()
	{
		Ohms arm = ConductorForm.CATENARY_WIRE.resistanceOver(new Blocks(150));
		TurnsRatio up = new TurnsRatio(4.0);
		List<NodeId> nodes = nodes(8);
		return new Circuit(nodes,
			List.of(new Conductor(nodes.get(2), nodes.get(4), arm), new Conductor(nodes.get(5), nodes.get(3), arm)),
			List.of(new Source(LIVE, RETURN, new Volts(128.0), Ohms.ZERO)),
			List.of(new Load(nodes.get(6), nodes.get(7), LoadClass.CONSTANT_POWER, new Watts(1024.0),
				new Volts(128.0))),
			List.of(new Transformer(LIVE, RETURN, nodes.get(2), nodes.get(3), up, LossFraction.NONE),
				new Transformer(nodes.get(4), nodes.get(5), nodes.get(6), nodes.get(7), up.inverted(),
					LossFraction.NONE)));
	}

	/** A thirty-six block busbar loop fed at one point and tapped at the point opposite, drawing one ampere. */
	private static Circuit ring(boolean closed)
	{
		List<NodeId> nodes = new ArrayList<>();
		nodes.add(RETURN);
		for (int step = 0; step < 36; step++)
		{
			nodes.add(ringNode(step));
		}

		List<Conductor> conductors = new ArrayList<>();
		for (int step = 0; step < (closed ? 36 : 35); step++)
		{
			conductors.add(new Conductor(ringNode(step), ringNode((step + 1) % 36), ConductorForm.BUSBAR.perBlock()));
		}

		return new Circuit(nodes, conductors, List.of(new Source(ringNode(0), RETURN, new Volts(1.0), Ohms.ZERO)),
			List.of(new Load(ringNode(18), RETURN, LoadClass.CONSTANT_CURRENT, new Watts(1.0), new Volts(1.0))),
			List.of());
	}

	/** The node a given step around the ring sits at. */
	private static NodeId ringNode(int step)
	{
		return new NodeId(step + 1);
	}
}
