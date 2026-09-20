package io.gifsync.septemcraft.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One circuit written out as equations and read back again. Nodes bolted together are one
 * electrical point; each galvanically joined part is measured from a point of its own; and every
 * other point's potential is an unknown, as is the current in each element that fixes a potential
 * rather than answering to one.
 *
 * <p>Built once for a solution and asked for readings as often as it takes. What is a point, and which
 * unknown each one is, cannot change while a circuit is being solved - only what its devices are
 * drawing.
 */
final class NodalAnalysis
{
	/** What something the solution does not carry an unknown for holds in place of one. */
	private static final int NO_UNKNOWN = -1;

	private final Circuit circuit;

	/** The runs of conductor that resist nothing, which are the joints the solve cannot see. */
	private final List<Conductor> joints;

	/** Which node each node is bolted to, which is the earliest node of its point. */
	private final int[] point;

	/** Which unknown each point's potential is, held against the node representing that point. */
	private final int[] unknownOfPoint;

	/** Which unknown each machine's own current is, where its windings resist nothing at all. */
	private final int[] unknownOfSource;

	/** Which unknown each transformer's primary current is. */
	private final int[] unknownOfTransformer;

	/**
	 * Everything about this circuit that does not change between passes, written down once. Each
	 * pass copies it and writes only what its devices are drawing onto the copy.
	 */
	private final LinearSystem invariant;

	/** Every joint that hangs a node, in the order to read them back: outermost first. */
	private final List<HangingJoint> sweep;

	/** Works out what the points of a circuit are and which unknown each thing about it is. */
	NodalAnalysis(Circuit circuit)
	{
		this.circuit = circuit;
		this.joints = circuit.conductors().stream().filter(conductor -> conductor.resistance().isNone()).toList();

		int nodes = circuit.nodes().size();
		this.point = merged(nodes, joints);
		this.unknownOfPoint = new int[nodes];
		Arrays.fill(unknownOfPoint, NO_UNKNOWN);

		int[] part = merged(nodes, circuit.elements());
		boolean[] measured = new boolean[nodes];
		int next = 0;
		for (int node = 0; node < nodes; node++)
		{
			if (point[node] != node)
			{
				continue;
			}

			if (measured[part[node]])
			{
				unknownOfPoint[node] = next++;
			}
			else
			{
				measured[part[node]] = true;
			}
		}

		this.unknownOfSource = new int[circuit.sources().size()];
		for (int index = 0; index < unknownOfSource.length; index++)
		{
			unknownOfSource[index] = circuit.sources().get(index).internalResistance().isNone() ? next++ : NO_UNKNOWN;
		}

		this.unknownOfTransformer = new int[circuit.transformers().size()];
		for (int index = 0; index < unknownOfTransformer.length; index++)
		{
			unknownOfTransformer[index] = next++;
		}

		this.invariant = new LinearSystem(next);
		stampConductors(invariant);
		stampSources(invariant);
		stampTransformers(invariant);

		this.sweep = span();
	}

	/** How many electrical points this circuit has, once everything bolted together counts as one. */
	int points()
	{
		int total = 0;
		for (int node = 0; node < point.length; node++)
		{
			if (point[node] == node)
			{
				total++;
			}
		}

		return total;
	}

	/** Whether two nodes are bolted together, and so are one point that nothing can sit across. */
	boolean joins(NodeId one, NodeId other)
	{
		return point[one.index()] == point[other.index()];
	}

	/**
	 * What this circuit reads with every device drawing what the potentials given have it draw, or
	 * nothing at all where the circuit does not determine its own answer. The potentials are given by
	 * node index, as a solve carries them from one pass to the next.
	 */
	Optional<CircuitReadings> readingsAt(double[] given)
	{
		LinearSystem system = invariant.copy();
		stampLoads(system, given);

		return system.solution().map(solved -> read(solved, given));
	}

	/** Writes every run of conductor that resists anything onto the system. */
	private void stampConductors(LinearSystem system)
	{
		for (Conductor conductor : circuit.conductors())
		{
			if (!conductor.resistance().isNone())
			{
				conductance(system, conductor.from(), conductor.to(), 1.0 / conductor.resistance().value());
			}
		}
	}

	/**
	 * Writes every machine onto the system. One whose windings resist becomes a current behind that
	 * resistance; one whose windings do not resist holds the potential across itself and carries a
	 * current of its own.
	 */
	private void stampSources(LinearSystem system)
	{
		for (int index = 0; index < circuit.sources().size(); index++)
		{
			Source source = circuit.sources().get(index);
			if (!source.internalResistance().isNone())
			{
				double conductance = 1.0 / source.internalResistance().value();
				conductance(system, source.from(), source.to(), conductance);
				injection(system, source.to(), source.from(), source.electromotiveForce().value() * conductance);
				continue;
			}

			int own = unknownOfSource[index];
			branch(system, own, source.from(), source.to(), 1.0);
			difference(system, own, source.to(), source.from(), 1.0);
			system.addConstant(own, source.electromotiveForce().value());
		}
	}

	/**
	 * Writes every device onto the system. One drawing by its own resistance is that resistance,
	 * which the solve then answers exactly; any other is the current the potentials given have it
	 * drawing, and a device below its minimum is not written at all.
	 */
	private void stampLoads(LinearSystem system, double[] given)
	{
		for (Load load : circuit.loads())
		{
			Volts terminals = potential(given, load.from(), load.to());
			if (load.isStalledAt(terminals))
			{
				continue;
			}

			if (load.behaviour().stampsAsResistance())
			{
				conductance(system, load.from(), load.to(), 1.0 / load.resistance().value());
			}
			else
			{
				injection(system, load.to(), load.from(), load.drawAt(terminals).value());
			}
		}
	}

	/**
	 * Writes every transformer onto the system: one row tying the potential across its secondary to
	 * the potential across its primary by the ratio, and a primary current that reappears in the
	 * secondary divided by that ratio and less the share the transformer takes.
	 */
	private void stampTransformers(LinearSystem system)
	{
		for (int index = 0; index < circuit.transformers().size(); index++)
		{
			Transformer transformer = circuit.transformers().get(index);
			Winding primary = transformer.primary();
			Winding secondary = transformer.secondary();
			int own = unknownOfTransformer[index];

			branch(system, own, primary.from(), primary.to(), 1.0);
			branch(system, own, secondary.from(), secondary.to(), -delivered(transformer));

			difference(system, own, secondary.from(), secondary.to(), 1.0);
			difference(system, own, primary.from(), primary.to(), -transformer.ratio().ratio());
		}
	}

	/** Reads the solved unknowns back out as the currents and potentials a circuit is carrying. */
	private CircuitReadings read(double[] solved, double[] given)
	{
		double[] voltages = new double[point.length];
		for (int node = 0; node < voltages.length; node++)
		{
			int unknown = unknownOfPoint[point[node]];
			voltages[node] = unknown < 0 ? 0.0 : solved[unknown];
		}

		double[] currents = new double[circuit.elements().size()];
		for (Conductor conductor : circuit.conductors())
		{
			if (!conductor.resistance().isNone())
			{
				currents[conductor.id().index()] = potential(voltages, conductor.from(), conductor.to())
					.over(conductor.resistance()).value();
			}
		}

		for (int index = 0; index < circuit.sources().size(); index++)
		{
			Source source = circuit.sources().get(index);
			currents[source.id().index()] = source.internalResistance().isNone()
				? solved[unknownOfSource[index]]
				: source.electromotiveForce().minus(potential(voltages, source.to(), source.from()))
					.over(source.internalResistance()).value();
		}

		for (Load load : circuit.loads())
		{
			currents[load.id().index()] = drawing(load, voltages, given).value();
		}

		for (int index = 0; index < circuit.transformers().size(); index++)
		{
			Transformer transformer = circuit.transformers().get(index);
			double primary = solved[unknownOfTransformer[index]];
			currents[transformer.primary().id().index()] = primary;
			currents[transformer.secondary().id().index()] = -delivered(transformer) * primary;
		}

		readJoints(currents);

		return new CircuitReadings(SolutionStatus.SOLVED, voltages, currents);
	}

	/**
	 * The current a device is drawing. One drawing by its own resistance answers the solved
	 * potentials exactly, since that resistance is what the solution was written around; any other draws
	 * what it was written in as, so that the current arriving at every node is the current the solution
	 * balanced.
	 */
	private Amperes drawing(Load load, double[] voltages, double[] given)
	{
		Volts terminals = potential(given, load.from(), load.to());
		if (load.isStalledAt(terminals))
		{
			return Amperes.ZERO;
		}

		return load.behaviour().stampsAsResistance()
			? potential(voltages, load.from(), load.to()).over(load.resistance())
			: load.drawAt(terminals);
	}

	/**
	 * Works out what each bolted joint is carrying, which the solution itself does not answer because
	 * the nodes a joint holds together were one point while it ran. Whatever a node takes from the
	 * rest of the circuit has to arrive along the joints at it, so the joints are read from the
	 * outside inwards. A joint closing a loop of them carries nothing: any current going round and
	 * round would satisfy the circuit equally, so none of them is the answer, and a joint the sweep
	 * never reaches keeps the nothing every reading starts at.
	 */
	private void readJoints(double[] currents)
	{
		if (joints.isEmpty())
		{
			return;
		}

		double[] arriving = new double[point.length];
		for (Element element : circuit.elements())
		{
			double carried = currents[element.id().index()];
			arriving[element.to().index()] += carried;
			arriving[element.from().index()] -= carried;
		}

		for (HangingJoint hanging : sweep)
		{
			double carried = arriving[hanging.node().index()];
			arriving[hanging.anchor().index()] += carried;
			currents[hanging.joint().id().index()] = hanging.outwards() ? carried : -carried;
		}
	}

	/**
	 * Walks the joints outwards from every node, giving back each joint that reached a node not
	 * already reached, in the order those joints are to be read: outermost first. A joint closing a
	 * loop reaches nothing new this way and so is never read, which is what leaves it carrying
	 * nothing.
	 */
	private List<HangingJoint> span()
	{
		List<HangingJoint> hung = new ArrayList<>();
		if (joints.isEmpty())
		{
			return List.copyOf(hung);
		}

		Map<NodeId, List<Conductor>> jointsAt = new HashMap<>();
		for (Conductor joint : joints)
		{
			jointsAt.computeIfAbsent(joint.from(), node -> new ArrayList<>()).add(joint);
			jointsAt.computeIfAbsent(joint.to(), node -> new ArrayList<>()).add(joint);
		}

		boolean[] seen = new boolean[point.length];
		Deque<NodeId> waiting = new ArrayDeque<>();
		for (NodeId node : circuit.nodes())
		{
			if (seen[node.index()])
			{
				continue;
			}

			seen[node.index()] = true;
			waiting.add(node);
			while (!waiting.isEmpty())
			{
				NodeId next = waiting.remove();
				for (Conductor joint : jointsAt.getOrDefault(next, List.of()))
				{
					NodeId other = joint.from().equals(next) ? joint.to() : joint.from();
					if (!seen[other.index()])
					{
						seen[other.index()] = true;
						hung.add(new HangingJoint(other, joint));
						waiting.add(other);
					}
				}
			}
		}

		Collections.reverse(hung);

		return List.copyOf(hung);
	}

	/** Writes a resistance between two nodes onto the system, which draws from one and pushes into the other. */
	private void conductance(LinearSystem system, NodeId from, NodeId to, double conductance)
	{
		int first = unknownOf(from);
		int second = unknownOf(to);
		if (first == second)
		{
			return;
		}

		if (first >= 0)
		{
			system.add(first, first, -conductance);
		}

		if (second >= 0)
		{
			system.add(second, second, -conductance);
		}

		if (first >= 0 && second >= 0)
		{
			system.add(first, second, conductance);
			system.add(second, first, conductance);
		}
	}

	/** Writes an element carrying a current of its own, running from one node to the other. */
	private void branch(LinearSystem system, int own, NodeId from, NodeId to, double share)
	{
		int first = unknownOf(from);
		int second = unknownOf(to);
		if (first >= 0)
		{
			system.add(first, own, -share);
		}

		if (second >= 0)
		{
			system.add(second, own, share);
		}
	}

	/** Writes a current already known, pushed into one node and drawn out of another. */
	private void injection(LinearSystem system, NodeId into, NodeId outOf, double current)
	{
		int first = unknownOf(into);
		int second = unknownOf(outOf);
		if (first == second)
		{
			return;
		}

		if (first >= 0)
		{
			system.addConstant(first, -current);
		}

		if (second >= 0)
		{
			system.addConstant(second, current);
		}
	}

	/** Writes a multiple of the potential between two nodes into one of the system's own rows. */
	private void difference(LinearSystem system, int row, NodeId from, NodeId to, double factor)
	{
		int first = unknownOf(from);
		int second = unknownOf(to);
		if (first >= 0)
		{
			system.add(row, first, factor);
		}

		if (second >= 0)
		{
			system.add(row, second, -factor);
		}
	}

	/** Which unknown a node's potential is, or a negative where its point is what others are measured from. */
	private int unknownOf(NodeId node)
	{
		return unknownOfPoint[point[node.index()]];
	}

	/** What a transformer's secondary carries for every ampere of its primary, less the share it takes. */
	private static double delivered(Transformer transformer)
	{
		return transformer.loss().remainder() / transformer.ratio().ratio();
	}

	/** The potential between two nodes of a set of potentials held by node index. */
	private static Volts potential(double[] voltages, NodeId from, NodeId to)
	{
		return new Volts(voltages[from.index()] - voltages[to.index()]);
	}

	/**
	 * Merges every node into the earliest node it is joined to, following only the joins the given
	 * elements make. Two blocks bolted together are one electrical point; two nodes with any element
	 * between them share metal, whatever that element is, and so are measured from the same place.
	 */
	private static int[] merged(int nodes, List<? extends Element> joins)
	{
		int[] merged = new int[nodes];
		for (int node = 0; node < nodes; node++)
		{
			merged[node] = node;
		}

		for (Element join : joins)
		{
			int one = rootOf(merged, join.from().index());
			int other = rootOf(merged, join.to().index());
			if (one < other)
			{
				merged[other] = one;
			}
			else
			{
				merged[one] = other;
			}
		}

		for (int node = 0; node < nodes; node++)
		{
			merged[node] = rootOf(merged, node);
		}

		return merged;
	}

	/** The earliest node a node has been merged into, walking up as far as the merging goes. */
	private static int rootOf(int[] merged, int node)
	{
		int root = node;
		while (merged[root] != root)
		{
			root = merged[root];
		}

		return root;
	}
}
