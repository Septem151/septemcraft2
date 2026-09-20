package io.gifsync.septemcraft.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles a circuit one node and one element at a time, which is how anything walking a world of
 * blocks finds them. Everything put in is handed back, so that a caller keeps its own map from the
 * block it came from to the element it became, and reads that element out of a solution later.
 *
 * <p>The first node minted becomes the circuit's reference.
 */
public final class CircuitBuilder
{
	private final List<NodeId> nodes = new ArrayList<>();

	private final List<Element> elements = new ArrayList<>();

	private final List<Transformer> transformers = new ArrayList<>();

	/** A node belonging to the circuit being built, which nothing yet joins. */
	public NodeId node()
	{
		NodeId node = new NodeId(nodes.size());
		nodes.add(node);

		return node;
	}

	/** Runs a conductor of the given resistance between two nodes. */
	public Conductor conductor(NodeId from, NodeId to, Ohms resistance)
	{
		Conductor conductor = new Conductor(nextElementId(), mine(from), mine(to), resistance);
		elements.add(conductor);

		return conductor;
	}

	/** Puts something driving the circuit between two nodes. */
	public Source source(NodeId from, NodeId to, Volts electromotiveForce, Ohms internalResistance)
	{
		Source source = new Source(nextElementId(), mine(from), mine(to), electromotiveForce, internalResistance);
		elements.add(source);

		return source;
	}

	/** Puts something drawing from the circuit between two nodes. */
	public Load load(NodeId from, NodeId to, LoadClass behaviour, Watts ratedPower, Volts ratedVoltage,
		Volts minimumVoltage)
	{
		Load load = new Load(nextElementId(), mine(from), mine(to), behaviour, ratedPower, ratedVoltage,
			minimumVoltage);
		elements.add(load);

		return load;
	}

	/** One side of a transformer, not yet coupled to anything. */
	public Winding winding(NodeId from, NodeId to)
	{
		Winding winding = new Winding(nextElementId(), mine(from), mine(to));
		elements.add(winding);

		return winding;
	}

	/** Couples two windings to each other. */
	public Transformer transformer(Winding primary, Winding secondary, TurnsRatio ratio, LossFraction loss)
	{
		Transformer transformer = new Transformer(primary, secondary, ratio, loss);
		transformers.add(transformer);

		return transformer;
	}

	/** The circuit as assembled so far. */
	public Circuit build()
	{
		if (nodes.isEmpty())
		{
			throw new IllegalStateException("A circuit is measured from its first node, and none has been minted");
		}

		return new AssembledCircuit(nodes, nodes.get(0), elements, ofKind(Conductor.class), ofKind(Source.class),
			ofKind(Load.class), transformers);
	}

	/** Every element of one kind put in, in the order it was, which is how a circuit reads its own. */
	private <T extends Element> List<T> ofKind(Class<T> kind)
	{
		return elements.stream().filter(kind::isInstance).map(kind::cast).toList();
	}

	/** What the next element put in is called, which is its position among the elements. */
	private ElementId nextElementId()
	{
		return new ElementId(elements.size());
	}

	/** Checks that a node is one this builder minted, since an element cannot reach a point elsewhere. */
	private NodeId mine(NodeId node)
	{
		if (node.index() >= nodes.size())
		{
			throw new IllegalArgumentException(node + " belongs to no circuit this builder is assembling");
		}

		return node;
	}
}
