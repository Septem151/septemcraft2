package io.gifsync.septemcraft.api;

/**
 * One side of a transformer. Current runs in at {@link #from()}, the live terminal, and out at
 * {@link #to()}, so a primary taking power reads positive and a secondary giving it out reads
 * negative.
 */
public record Winding(ElementId id, NodeId from, NodeId to) implements Element
{
	/** Checks that a winding is across two nodes. */
	public Winding
	{
		if (from.equals(to))
		{
			throw new IllegalArgumentException("A winding is across two nodes, not " + from + " and itself");
		}
	}
}
