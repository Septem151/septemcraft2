package io.gifsync.septemcraft.api;

/**
 * Assembles a circuit one node and one element at a time, which is how anything walking a world of
 * blocks finds them. Everything put in is handed back, so that a caller keeps its own map from the
 * block it came from to the element it became, and reads that element out of a solution later.
 *
 * <p>The first node minted becomes the circuit's reference.
 */
// Every method here throws until the builder is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
// TODO: Remove once implemented.
@SuppressWarnings("DoNotCallSuggester")
public final class CircuitBuilder
{
	/** A node belonging to the circuit being built, which nothing yet joins. */
	public NodeId node()
	{
		throw new UnsupportedOperationException("CircuitBuilder.node() is not implemented.");
	}

	/** Runs a conductor of the given resistance between two nodes. */
	public Conductor conductor(NodeId from, NodeId to, Ohms resistance)
	{
		throw new UnsupportedOperationException("CircuitBuilder.conductor(..) is not implemented.");
	}

	/** Puts something driving the circuit between two nodes. */
	public Source source(NodeId from, NodeId to, Volts electromotiveForce, Ohms internalResistance)
	{
		throw new UnsupportedOperationException("CircuitBuilder.source(..) is not implemented.");
	}

	/** Puts something drawing from the circuit between two nodes. */
	public Load load(NodeId from, NodeId to, LoadClass behaviour, Watts ratedPower, Volts ratedVoltage,
		Volts minimumVoltage)
	{
		throw new UnsupportedOperationException("CircuitBuilder.load(..) is not implemented.");
	}

	/** One side of a transformer, not yet coupled to anything. */
	public Winding winding(NodeId from, NodeId to)
	{
		throw new UnsupportedOperationException("CircuitBuilder.winding(..) is not implemented.");
	}

	/** Couples two windings to each other. */
	public Transformer transformer(Winding primary, Winding secondary, TurnsRatio ratio, LossFraction loss)
	{
		throw new UnsupportedOperationException("CircuitBuilder.transformer(..) is not implemented.");
	}

	/** The circuit as assembled so far. */
	public Circuit build()
	{
		throw new UnsupportedOperationException("CircuitBuilder.build() is not implemented.");
	}
}
