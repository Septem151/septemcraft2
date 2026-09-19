package io.gifsync.septemcraft.api;

/** A length, counted in blocks. */
public record Blocks(int count)
{
	/** No length at all. */
	public static final Blocks NONE = new Blocks(0);
}
