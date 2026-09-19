package io.gifsync.septemcraft.electrification.circuit;

/**
 * How many volts a transformer's secondary makes of one volt on its primary. A ratio above one steps
 * up and a ratio below one steps down; where a coil's windings get it from is the coil's business.
 */
record TurnsRatio(double ratio)
{
	/** Checks that a ratio is a number that can be divided by, which no ratio of zero is. */
	TurnsRatio
	{
		if (!Double.isFinite(ratio) || ratio == 0.0)
		{
			throw new IllegalArgumentException("A turns ratio is a finite non-zero number, not " + ratio);
		}
	}

	/** This ratio the other way about, which is what a matching transformer is wound to. */
	TurnsRatio inverted()
	{
		return new TurnsRatio(1.0 / ratio);
	}

	/** What a potential on a primary becomes on the secondary this ratio describes. */
	Volts applyTo(Volts primary)
	{
		return new Volts(primary.value() * ratio);
	}
}
