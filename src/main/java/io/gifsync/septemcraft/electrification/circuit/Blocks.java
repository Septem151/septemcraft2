package io.gifsync.septemcraft.electrification.circuit;

/** A length of conductor, counted in blocks. */
record Blocks(int count)
{
	/** Checks that a run of conductor is not a negative length. */
	Blocks
	{
		if (count < 0)
		{
			throw new IllegalArgumentException("A run of conductor is at least zero blocks, not " + count);
		}
	}
}
