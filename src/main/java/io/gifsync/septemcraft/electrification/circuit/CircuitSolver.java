package io.gifsync.septemcraft.electrification.circuit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Solves a circuit by modified nodal analysis: one node of each galvanically joined part is the
 * reference, every other node's voltage is an unknown, and so is the current in every element that
 * fixes a voltage rather than answering to one. A constant-power load answers to the voltage it is
 * given, so the solve is repeated from an unloaded start until the voltages stop moving.
 */
final class CircuitSolver
{
	/**
	 * Solves a circuit from an unloaded start, which is the start that settles a constant-power load on
	 * its high-voltage answer rather than on the unstable one that also satisfies it.
	 */
	Solution solve(Circuit circuit)
	{
		Map<NodeId, Volts> unloaded = new HashMap<>();
		for (NodeId node : circuit.nodes())
		{
			unloaded.put(node, Volts.ZERO);
		}

		return solveFrom(circuit, unloaded);
	}

	/**
	 * Solves a circuit from a given set of node voltages rather than from an unloaded start, which is
	 * how a solve is aimed at one of a nonlinear circuit's several answers.
	 */
	Solution solveFrom(Circuit circuit, Map<NodeId, Volts> start)
	{
		NodeContraction contraction = circuit.contraction();
		if (contraction.merged().size() > ElectricalConstants.MAX_NODES_PER_CIRCUIT)
		{
			return dead(circuit, SolutionStatus.TOO_LARGE);
		}

		if (!circuit.isEnergised())
		{
			return dead(circuit, SolutionStatus.DE_ENERGISED);
		}

		Map<NodeId, Volts> guesses = new HashMap<>(start);
		Optional<Solution> first = attempt(circuit, contraction, guesses);
		if (first.isEmpty())
		{
			return dead(circuit, SolutionStatus.SINGULAR);
		}

		Solution previous = first.get();
		if (!circuit.isNonlinear())
		{
			return previous;
		}

		relax(guesses, previous, circuit.nodes());
		for (int iteration = 1; iteration <= ElectricalConstants.MAX_ITERATIONS; iteration++)
		{
			Optional<Solution> next = attempt(circuit, contraction, guesses);
			if (next.isEmpty())
			{
				return dead(circuit, SolutionStatus.SINGULAR);
			}

			Solution current = next.get();
			if (hasSettled(previous, current, circuit.nodes()))
			{
				return current;
			}

			previous = current;
			relax(guesses, current, circuit.nodes());
		}

		return new Solution(SolutionStatus.NOT_CONVERGED, previous.voltages(), previous.conductorCurrents(),
			previous.sourceCurrents(), previous.loadCurrents(), previous.transformerCurrents());
	}

	/** One linear solve, with every constant-power load held at the current its guessed voltage implies. */
	private static Optional<Solution> attempt(Circuit circuit, NodeContraction contraction,
		Map<NodeId, Volts> guesses)
	{
		Map<NodeId, Integer> unknowns = unknownsOf(circuit, contraction);
		Map<Source, Integer> sourceUnknowns = new LinkedHashMap<>();
		Map<Transformer, Integer> transformerUnknowns = new LinkedHashMap<>();

		int next = (int) unknowns.values().stream().filter(index -> index >= 0).distinct().count();
		for (Source source : circuit.sources())
		{
			if (source.internalResistance().isNone())
			{
				sourceUnknowns.put(source, next++);
			}
		}

		for (Transformer transformer : circuit.transformers())
		{
			transformerUnknowns.put(transformer, next++);
		}

		LinearSystem system = new LinearSystem(next);
		for (Conductor conductor : circuit.conductors())
		{
			if (!conductor.resistance().isNone())
			{
				stampConductance(system, unknowns, conductor.from(), conductor.to(),
					1.0 / conductor.resistance().value());
			}
		}

		for (Source source : circuit.sources())
		{
			stampSource(system, unknowns, sourceUnknowns, source);
		}

		for (Load load : circuit.loads())
		{
			stampLoad(system, unknowns, load, guesses);
		}

		for (Transformer transformer : circuit.transformers())
		{
			stampTransformer(system, unknowns, transformerUnknowns, transformer);
		}

		return system.solution()
			.map(values -> read(circuit, unknowns, sourceUnknowns, transformerUnknowns, values));
	}

	/**
	 * Numbers the unknown node voltages, giving each galvanically joined part a reference of its own
	 * and every merged node the number its representative carries.
	 */
	private static Map<NodeId, Integer> unknownsOf(Circuit circuit, NodeContraction contraction)
	{
		Map<NodeId, Integer> byRepresentative = new LinkedHashMap<>();
		int next = 0;
		for (List<NodeId> group : circuit.galvanicGroups())
		{
			boolean grounded = false;
			for (NodeId node : group)
			{
				NodeId representative = contraction.representativeOf(node);
				if (byRepresentative.containsKey(representative))
				{
					continue;
				}

				if (grounded)
				{
					byRepresentative.put(representative, next++);
				}
				else
				{
					byRepresentative.put(representative, -1);
					grounded = true;
				}
			}
		}

		Map<NodeId, Integer> unknowns = new LinkedHashMap<>();
		for (NodeId node : circuit.nodes())
		{
			unknowns.put(node, requireIndex(byRepresentative, contraction.representativeOf(node)));
		}

		return unknowns;
	}

	/** Stamps a resistance between two nodes onto the system. */
	private static void stampConductance(LinearSystem system, Map<NodeId, Integer> unknowns, NodeId from, NodeId to,
		double conductance)
	{
		int first = indexOf(unknowns, from);
		int second = indexOf(unknowns, to);
		if (first == second)
		{
			return;
		}

		if (first >= 0)
		{
			system.add(first, first, conductance);
		}

		if (second >= 0)
		{
			system.add(second, second, conductance);
		}

		if (first >= 0 && second >= 0)
		{
			system.add(first, second, -conductance);
			system.add(second, first, -conductance);
		}
	}

	/** Stamps a current pushed into the first node and out of the second onto the system. */
	private static void stampInjection(LinearSystem system, Map<NodeId, Integer> unknowns, NodeId into, NodeId outOf,
		double current)
	{
		int first = indexOf(unknowns, into);
		int second = indexOf(unknowns, outOf);
		if (first == second)
		{
			return;
		}

		if (first >= 0)
		{
			system.addConstant(first, current);
		}

		if (second >= 0)
		{
			system.addConstant(second, -current);
		}
	}

	/**
	 * Stamps a machine: one with winding resistance becomes a current behind that resistance, and one
	 * without fixes the voltage across it and carries a current of its own as an unknown.
	 */
	private static void stampSource(LinearSystem system, Map<NodeId, Integer> unknowns,
		Map<Source, Integer> sourceUnknowns, Source source)
	{
		if (!source.internalResistance().isNone())
		{
			double conductance = 1.0 / source.internalResistance().value();
			stampConductance(system, unknowns, source.positive(), source.negative(), conductance);
			stampInjection(system, unknowns, source.positive(), source.negative(),
				source.electromotiveForce().value() * conductance);
			return;
		}

		int branch = requireIndex(sourceUnknowns, source);
		int positive = indexOf(unknowns, source.positive());
		int negative = indexOf(unknowns, source.negative());
		if (positive >= 0)
		{
			system.add(positive, branch, 1.0);
			system.add(branch, positive, 1.0);
		}

		if (negative >= 0)
		{
			system.add(negative, branch, -1.0);
			system.add(branch, negative, -1.0);
		}

		system.addConstant(branch, source.electromotiveForce().value());
	}

	/**
	 * Stamps a device: a constant-resistance one as the resistance it presents, and any other as the
	 * current it draws at the voltage it is being given.
	 */
	private static void stampLoad(LinearSystem system, Map<NodeId, Integer> unknowns, Load load,
		Map<NodeId, Volts> guesses)
	{
		if (load.loadClass() == LoadClass.CONSTANT_RESISTANCE)
		{
			stampConductance(system, unknowns, load.from(), load.to(), 1.0 / load.resistance().value());
			return;
		}

		Volts terminal = guessAt(guesses, load.from()).minus(guessAt(guesses, load.to()));
		stampInjection(system, unknowns, load.to(), load.from(), load.drawAt(terminal).value());
	}

	/**
	 * Stamps a transformer: two rows tying the secondary's voltage to the primary's by the ratio, and
	 * a primary current that reappears in the secondary divided by that ratio and less the loss.
	 */
	private static void stampTransformer(LinearSystem system, Map<NodeId, Integer> unknowns,
		Map<Transformer, Integer> transformerUnknowns, Transformer transformer)
	{
		int branch = requireIndex(transformerUnknowns, transformer);
		double ratio = transformer.ratio().ratio();
		double delivered = transformer.loss().remainder() / ratio;

		int primaryPositive = indexOf(unknowns, transformer.primaryPositive());
		int primaryNegative = indexOf(unknowns, transformer.primaryNegative());
		int secondaryPositive = indexOf(unknowns, transformer.secondaryPositive());
		int secondaryNegative = indexOf(unknowns, transformer.secondaryNegative());

		if (primaryPositive >= 0)
		{
			system.add(primaryPositive, branch, 1.0);
			system.add(branch, primaryPositive, -ratio);
		}

		if (primaryNegative >= 0)
		{
			system.add(primaryNegative, branch, -1.0);
			system.add(branch, primaryNegative, ratio);
		}

		if (secondaryPositive >= 0)
		{
			system.add(secondaryPositive, branch, -delivered);
			system.add(branch, secondaryPositive, 1.0);
		}

		if (secondaryNegative >= 0)
		{
			system.add(secondaryNegative, branch, delivered);
			system.add(branch, secondaryNegative, -1.0);
		}
	}

	/** Reads the solved unknowns back out as the readings a circuit carries. */
	private static Solution read(Circuit circuit, Map<NodeId, Integer> unknowns, Map<Source, Integer> sourceUnknowns,
		Map<Transformer, Integer> transformerUnknowns, double[] values)
	{
		Map<NodeId, Volts> voltages = new HashMap<>();
		for (NodeId node : circuit.nodes())
		{
			int index = indexOf(unknowns, node);
			voltages.put(node, index < 0 ? Volts.ZERO : new Volts(values[index]));
		}

		Map<Conductor, Amperes> conductorCurrents = new HashMap<>();
		for (Conductor conductor : circuit.conductors())
		{
			conductorCurrents.put(conductor, conductor.resistance().isNone()
				? Amperes.ZERO
				: potential(voltages, conductor.from(), conductor.to()).over(conductor.resistance()));
		}

		Map<Source, Amperes> sourceCurrents = new HashMap<>();
		for (Source source : circuit.sources())
		{
			Volts terminal = potential(voltages, source.positive(), source.negative());
			sourceCurrents.put(source, source.internalResistance().isNone()
				? new Amperes(-values[requireIndex(sourceUnknowns, source)])
				: source.electromotiveForce().minus(terminal).over(source.internalResistance()));
		}

		Map<Load, Amperes> loadCurrents = new HashMap<>();
		for (Load load : circuit.loads())
		{
			loadCurrents.put(load, load.drawAt(potential(voltages, load.from(), load.to())));
		}

		Map<Transformer, Amperes> transformerCurrents = new HashMap<>();
		for (Transformer transformer : circuit.transformers())
		{
			transformerCurrents.put(transformer, new Amperes(values[requireIndex(transformerUnknowns, transformer)]));
		}

		return new Solution(SolutionStatus.SOLVED, voltages, conductorCurrents, sourceCurrents, loadCurrents,
			transformerCurrents);
	}

	/** Moves every guess part of the way towards what the last solve said, which is what damps an oscillation. */
	private static void relax(Map<NodeId, Volts> guesses, Solution solution, List<NodeId> nodes)
	{
		for (NodeId node : nodes)
		{
			Volts guess = guessAt(guesses, node);
			Volts solved = solution.voltageAt(node);
			guesses.put(node, new Volts(guess.value()
				+ ElectricalConstants.RELAXATION_FACTOR * (solved.value() - guess.value())));
		}
	}

	/** Whether two solves agree closely enough for the circuit to count as settled. */
	private static boolean hasSettled(Solution previous, Solution current, List<NodeId> nodes)
	{
		for (NodeId node : nodes)
		{
			if (current.voltageAt(node)
				.distanceFrom(previous.voltageAt(node)) >= ElectricalConstants.CONVERGENCE_TOLERANCE)
			{
				return false;
			}
		}

		return true;
	}

	/** A circuit reading as nothing anywhere, which is what a circuit that cannot be solved reads as. */
	private static Solution dead(Circuit circuit, SolutionStatus status)
	{
		Map<NodeId, Volts> voltages = new HashMap<>();
		for (NodeId node : circuit.nodes())
		{
			voltages.put(node, Volts.ZERO);
		}

		Map<Conductor, Amperes> conductorCurrents = new HashMap<>();
		for (Conductor conductor : circuit.conductors())
		{
			conductorCurrents.put(conductor, Amperes.ZERO);
		}

		Map<Source, Amperes> sourceCurrents = new HashMap<>();
		for (Source source : circuit.sources())
		{
			sourceCurrents.put(source, Amperes.ZERO);
		}

		Map<Load, Amperes> loadCurrents = new HashMap<>();
		for (Load load : circuit.loads())
		{
			loadCurrents.put(load, Amperes.ZERO);
		}

		Map<Transformer, Amperes> transformerCurrents = new HashMap<>();
		for (Transformer transformer : circuit.transformers())
		{
			transformerCurrents.put(transformer, Amperes.ZERO);
		}

		return new Solution(status, voltages, conductorCurrents, sourceCurrents, loadCurrents, transformerCurrents);
	}

	/** The potential between two nodes of a half-built reading. */
	private static Volts potential(Map<NodeId, Volts> voltages, NodeId from, NodeId to)
	{
		Volts first = voltages.get(from);
		Volts second = voltages.get(to);
		if (first == null || second == null)
		{
			throw new IllegalStateException("A solved circuit holds a voltage for every node it names");
		}

		return first.minus(second);
	}

	/** The guess standing for a node, which every node of the circuit has. */
	private static Volts guessAt(Map<NodeId, Volts> guesses, NodeId node)
	{
		Volts guess = guesses.get(node);
		if (guess == null)
		{
			throw new IllegalArgumentException("A start holds a voltage for every node, and none for " + node);
		}

		return guess;
	}

	/** Which unknown a node's voltage is, or the reference when it is the one node not solved for. */
	private static int indexOf(Map<NodeId, Integer> unknowns, NodeId node)
	{
		Integer index = unknowns.get(node);
		if (index == null)
		{
			throw new IllegalArgumentException(node + " is not a node this system was built over");
		}

		return index;
	}

	/** Which unknown an element's own current is. */
	private static <K> int requireIndex(Map<K, Integer> indices, K element)
	{
		Integer index = indices.get(element);
		if (index == null)
		{
			throw new IllegalStateException("An element carrying its own current was never given an unknown");
		}

		return index;
	}
}
