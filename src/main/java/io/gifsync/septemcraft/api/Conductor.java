package io.gifsync.septemcraft.api;

/** A run of conductor between two nodes, which may resist nothing at all. */
public record Conductor(ElementId id, NodeId from, NodeId to, Ohms resistance) implements Element
{
	/** Checks that a run joins two nodes and that it resists something or nothing rather than less. */
	public Conductor
	{
		if (from.equals(to))
		{
			throw new IllegalArgumentException("A run joins two nodes, not " + from + " to itself");
		}

		if (resistance.value() < 0.0)
		{
			throw new IllegalArgumentException("A run resists something or nothing, not " + resistance);
		}
	}
}
