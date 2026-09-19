package io.gifsync.septemcraft.electrification.circuit;

/** What a circuit calls one of its nodes - a point every conductor meeting there shares. */
record NodeId(int index)
{
	/** Checks that a node is named by a position in the circuit's own list. */
	NodeId
	{
		if (index < 0)
		{
			throw new IllegalArgumentException("A node is named by a position from zero up, not " + index);
		}
	}
}
