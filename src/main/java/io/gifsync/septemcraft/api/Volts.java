package io.gifsync.septemcraft.api;

/** An electrical potential, or a difference between two of them. */
public record Volts(double value)
{
	/** No potential at all, which is what an unpowered node reads. */
	public static final Volts ZERO = new Volts(0.0);

	/** Checks that a potential is a number, which a potential of either sign is. */
	public Volts
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A potential is finite, not " + value);
		}
	}

	/** The current this potential drives through a resistance. */
	public Amperes over(Ohms resistance)
	{
		return new Amperes(value / resistance.value());
	}

	/** The resistance that would draw the given current at this potential. */
	public Ohms over(Amperes current)
	{
		return new Ohms(value / current.value());
	}

	/** The power carried by the given current at this potential. */
	public Watts times(Amperes current)
	{
		return new Watts(value * current.value());
	}

	/** The potential left when another is taken from this one. */
	Volts minus(Volts other)
	{
		return new Volts(value - other.value);
	}
}
