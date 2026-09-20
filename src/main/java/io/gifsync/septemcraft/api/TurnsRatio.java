package io.gifsync.septemcraft.api;

/** How many turns a transformer's secondary carries for every turn of its primary. */
public record TurnsRatio(double ratio)
{
	/** Checks that a ratio is a number that can be divided by, which no ratio of none is. */
	public TurnsRatio
	{
		if (!Double.isFinite(ratio) || ratio == 0.0)
		{
			throw new IllegalArgumentException("A turns ratio is a finite number of turns, not " + ratio);
		}
	}

	/** The same transformer read from its other side. */
	public TurnsRatio inverted()
	{
		return new TurnsRatio(1.0 / ratio);
	}

	/** The potential this ratio turns the given one into. */
	public Volts applyTo(Volts potential)
	{
		return new Volts(potential.value() * ratio);
	}
}
