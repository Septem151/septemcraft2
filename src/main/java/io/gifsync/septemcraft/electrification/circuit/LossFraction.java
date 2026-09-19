package io.gifsync.septemcraft.electrification.circuit;

/** The share of the power crossing a transformer that the transformer keeps for itself. */
record LossFraction(double fraction)
{
	/** Losing nothing, which is what an ideal element does. */
	static final LossFraction NONE = new LossFraction(0.0);

	/** Checks that a loss is a share of what crosses, and not all of it. */
	LossFraction
	{
		if (!(fraction >= 0.0) || fraction >= 1.0)
		{
			throw new IllegalArgumentException("A loss falls between none and all, not " + fraction);
		}
	}

	/** What is left of a unit of power once this loss is taken. */
	double remainder()
	{
		return 1.0 - fraction;
	}
}
