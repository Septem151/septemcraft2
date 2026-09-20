package io.gifsync.septemcraft.api;

/** A length, counted in blocks. */
public record Blocks(int count)
{
	/** No length at all. */
	public static final Blocks NONE = new Blocks(0);

	/** Checks that a length is a count of blocks, and that no run is shorter than none. */
	public Blocks
	{
		if (count < 0)
		{
			throw new IllegalArgumentException("A run is at least no blocks long, not " + count);
		}
	}
}
