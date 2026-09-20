package io.gifsync.septemcraft.api;

/** A resistance. */
public record Ohms(double value)
{
	/** No resistance at all, which is what a bolted joint between two blocks offers. */
	public static final Ohms ZERO = new Ohms(0.0);

	/** Checks that a resistance is a number, which a resistance of either sign is. */
	public Ohms
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A resistance is finite, not " + value);
		}
	}

	/** What a run of this many blocks of something resisting this much comes to. */
	Ohms times(Blocks length)
	{
		return new Ohms(value * length.count());
	}

	/** Whether this is no resistance at all, which is what a joint collapsed before a solve offers. */
	boolean isNone()
	{
		return value == 0.0;
	}
}
