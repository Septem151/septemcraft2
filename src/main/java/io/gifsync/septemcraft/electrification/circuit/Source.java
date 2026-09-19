package io.gifsync.septemcraft.electrification.circuit;

/**
 * A machine driving the circuit: an electromotive force behind a winding resistance. Its current is
 * positive when it delivers out of its positive terminal and negative when its own grid drives it.
 */
record Source(NodeId positive, NodeId negative, Volts electromotiveForce, Ohms internalResistance)
{
	/** Checks that a source sits between two different nodes. */
	Source
	{
		if (positive.equals(negative))
		{
			throw new IllegalArgumentException("A source sits between two nodes, not " + positive + " and itself");
		}
	}

	/** Whether this source is turning, which a source at no electromotive force is not. */
	boolean isRunning()
	{
		return electromotiveForce.value() != 0.0;
	}
}
