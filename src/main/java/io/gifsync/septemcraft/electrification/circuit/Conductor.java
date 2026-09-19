package io.gifsync.septemcraft.electrification.circuit;

/**
 * A resistive path between two nodes. Its current is positive when it flows from the first node to
 * the second; a conductor of no resistance is contracted away before the solve and reports none.
 */
record Conductor(NodeId from, NodeId to, Ohms resistance)
{
	/** Checks that a conductor joins two different nodes. */
	Conductor
	{
		if (from.equals(to))
		{
			throw new IllegalArgumentException("A conductor joins two nodes, not " + from + " to itself");
		}
	}

	/** The power this conductor burns carrying a current. */
	Watts lossAt(Amperes current)
	{
		return current.dissipatedIn(resistance);
	}
}
