package io.gifsync.septemcraft.api;

/** A current, signed by the direction it runs in. */
public record Amperes(double value)
{
	/** No current at all. */
	public static final Amperes ZERO = new Amperes(0.0);

	/** Checks that a current is a number, which a current of either sign is. */
	public Amperes
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A current is finite, not " + value);
		}
	}

	/** The potential this current raises across a resistance. */
	public Volts times(Ohms resistance)
	{
		return new Volts(value * resistance.value());
	}

	/** The power this current burns in a resistance, which it does whichever way it runs. */
	public Watts dissipatedIn(Ohms resistance)
	{
		return new Watts(value * value * resistance.value());
	}
}
