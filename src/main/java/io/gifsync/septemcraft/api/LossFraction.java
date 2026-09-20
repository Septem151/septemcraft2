package io.gifsync.septemcraft.api;

/** The share of what crosses something that does not come out the other side. */
public record LossFraction(double fraction)
{
	/** A crossing that loses nothing. */
	public static final LossFraction NONE = new LossFraction(0.0);

	/** Checks that a loss is a share of what crosses, and not all of it. */
	public LossFraction
	{
		if (!(fraction >= 0.0) || fraction >= 1.0)
		{
			throw new IllegalArgumentException("A loss falls between none and all of what crosses, not " + fraction);
		}
	}

	/** The share of what crosses that does come out the other side. */
	double remainder()
	{
		return 1.0 - fraction;
	}
}
