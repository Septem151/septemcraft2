package io.gifsync.septemcraft.electrification.circuit;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Checks what a circuit refuses to be built as, and which of its nodes it merges before anything solves it. */
class CircuitTest
{
	private static final double EXACT = 1.0e-12;

	@Test
	void aChainOfResistancelessConductorsIsOneNode()
	{
		List<NodeId> nodes = List.of(new NodeId(0), new NodeId(1), new NodeId(2), new NodeId(3));
		Circuit circuit = new Circuit(nodes,
			List.of(new Conductor(nodes.get(0), nodes.get(1), Ohms.ZERO),
				new Conductor(nodes.get(1), nodes.get(2), Ohms.ZERO),
				new Conductor(nodes.get(2), nodes.get(3), Ohms.ZERO)),
			List.of(), List.of(), List.of());

		NodeContraction contraction = circuit.contraction();

		assertEquals(List.of(nodes.get(0)), contraction.merged());
		for (NodeId node : nodes)
		{
			assertEquals(nodes.get(0), contraction.representativeOf(node));
		}
	}

	@Test
	void aResistanceLeftStandingKeepsItsOwnNodes()
	{
		List<NodeId> nodes = List.of(new NodeId(0), new NodeId(1), new NodeId(2));
		Circuit circuit = new Circuit(nodes,
			List.of(new Conductor(nodes.get(0), nodes.get(1), Ohms.ZERO),
				new Conductor(nodes.get(1), nodes.get(2), new Ohms(4.0))),
			List.of(), List.of(), List.of());

		assertEquals(List.of(nodes.get(0), nodes.get(2)), circuit.contraction().merged());
	}

	@Test
	void aBoltedJointIsNotAResistance()
	{
		Solution jointed = new CircuitSolver().solve(dividerThrough(Ohms.ZERO));
		Solution welded = new CircuitSolver().solve(dividerThrough(new Ohms(2.0)));

		assertEquals(4.0, jointed.voltageAt(new NodeId(3)).value(), EXACT);
		assertEquals(12.0 * 4.0 / 14.0, welded.voltageAt(new NodeId(3)).value(), 1.0e-9);
	}

	@Test
	void aCircuitWithNoSourceIsLegalAndReadsAsNothing()
	{
		List<NodeId> nodes = List.of(new NodeId(0), new NodeId(1));
		Conductor conductor = new Conductor(nodes.get(0), nodes.get(1), new Ohms(4.0));
		Circuit circuit = new Circuit(nodes, List.of(conductor), List.of(), List.of(), List.of());

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(SolutionStatus.DE_ENERGISED, solution.status());
		assertEquals(0.0, solution.voltageAt(nodes.get(1)).value(), EXACT);
		assertEquals(0.0, solution.through(conductor).value(), EXACT);
	}

	@Test
	void twoUnjoinedPartsNeitherSeeNorFeedEachOther()
	{
		List<NodeId> nodes = List.of(new NodeId(0), new NodeId(1), new NodeId(2), new NodeId(3));
		Circuit circuit = new Circuit(nodes, List.of(),
			List.of(new Source(nodes.get(1), nodes.get(0), new Volts(128.0), new Ohms(0.5)),
				new Source(nodes.get(3), nodes.get(2), new Volts(64.0), new Ohms(0.5))),
			List.of(new Load(nodes.get(1), nodes.get(0), LoadClass.CONSTANT_RESISTANCE, new Watts(1024.0),
				new Volts(128.0)),
				new Load(nodes.get(3), nodes.get(2), LoadClass.CONSTANT_RESISTANCE, new Watts(1024.0),
					new Volts(128.0))),
			List.of());

		Solution solution = new CircuitSolver().solve(circuit);

		assertEquals(0.0, solution.voltageAt(nodes.get(0)).value(), EXACT);
		assertEquals(128.0 * 16.0 / 16.5, solution.voltageAt(nodes.get(1)).value(), 1.0e-9);
		assertEquals(0.0, solution.voltageAt(nodes.get(2)).value(), EXACT);
		assertEquals(64.0 * 16.0 / 16.5, solution.voltageAt(nodes.get(3)).value(), 1.0e-9);
	}

	@Test
	void anElementNamingANodeTheCircuitDoesNotHaveIsRefused()
	{
		List<NodeId> nodes = List.of(new NodeId(0), new NodeId(1));

		assertThrows(IllegalArgumentException.class, () -> new Circuit(nodes,
			List.of(new Conductor(nodes.get(0), new NodeId(9), new Ohms(1.0))), List.of(), List.of(), List.of()));
	}

	@Test
	void twoConductorsAlikeBetweenTheSameNodesAreRefused()
	{
		List<NodeId> nodes = List.of(new NodeId(0), new NodeId(1));
		Conductor conductor = new Conductor(nodes.get(0), nodes.get(1), new Ohms(1.0));

		assertThrows(IllegalArgumentException.class,
			() -> new Circuit(nodes, List.of(conductor, conductor), List.of(), List.of(), List.of()));
	}

	@Test
	void aCircuitLargerThanTheSolverIsSizedForReadsAsNothing()
	{
		int count = ElectricalConstants.MAX_NODES_PER_CIRCUIT + 2;
		List<NodeId> nodes = IntStream.range(0, count).mapToObj(NodeId::new).toList();
		List<Conductor> conductors = IntStream.range(0, count - 1)
			.mapToObj(index -> new Conductor(nodes.get(index), nodes.get(index + 1), new Ohms(1.0)))
			.toList();
		Circuit circuit = new Circuit(nodes, conductors,
			List.of(new Source(nodes.get(0), nodes.get(count - 1), new Volts(128.0), Ohms.ZERO)), List.of(),
			List.of());

		assertEquals(SolutionStatus.TOO_LARGE, new CircuitSolver().solve(circuit).status());
	}

	/** A twelve-volt divider whose upper arm is split in two by a joint of the given resistance. */
	private static Circuit dividerThrough(Ohms joint)
	{
		List<NodeId> nodes = List.of(new NodeId(0), new NodeId(1), new NodeId(2), new NodeId(3));
		return new Circuit(nodes,
			List.of(new Conductor(nodes.get(1), nodes.get(2), new Ohms(8.0)),
				new Conductor(nodes.get(2), nodes.get(3), joint)),
			List.of(new Source(nodes.get(1), nodes.get(0), new Volts(12.0), Ohms.ZERO)),
			List.of(new Load(nodes.get(3), nodes.get(0), LoadClass.CONSTANT_RESISTANCE, new Watts(36.0),
				new Volts(12.0))),
			List.of());
	}
}
