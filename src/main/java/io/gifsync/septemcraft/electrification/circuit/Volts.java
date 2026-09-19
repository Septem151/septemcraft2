package io.gifsync.septemcraft.electrification.circuit;

/** A potential difference, of either sign. */
record Volts(double value)
{
	/** No potential at all, which is what an unenergised node sits at. */
	static final Volts ZERO = new Volts(0.0);

	/** Checks that a potential is a number, which a potential of either sign is. */
	Volts
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A potential is finite, not " + value);
		}
	}

	/** The potential this one and another add up to. */
	Volts plus(Volts other)
	{
		return new Volts(value + other.value);
	}

	/** The potential left when another is taken from this one. */
	Volts minus(Volts other)
	{
		return new Volts(value - other.value);
	}

	/** This potential with its sign reversed. */
	Volts negated()
	{
		return new Volts(-value);
	}

	/** The current this potential drives through a resistance. */
	Amperes over(Ohms resistance)
	{
		return new Amperes(value / resistance.value());
	}

	/** The resistance a current at this potential implies. */
	Ohms over(Amperes current)
	{
		return new Ohms(value / current.value());
	}

	/** The power this potential delivers at a current. */
	Watts times(Amperes current)
	{
		return new Watts(value * current.value());
	}

	/** How far this potential sits from another, whichever way round they are. */
	double distanceFrom(Volts other)
	{
		return Math.abs(value - other.value);
	}
}
