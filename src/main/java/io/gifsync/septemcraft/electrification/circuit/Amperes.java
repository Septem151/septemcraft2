package io.gifsync.septemcraft.electrification.circuit;

/** A current, of either sign - a negative current is one flowing the other way. */
record Amperes(double value)
{
	/** No current at all, which is what an open branch carries. */
	static final Amperes ZERO = new Amperes(0.0);

	/** Checks that a current is a number, which a current of either sign is. */
	Amperes
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A current is finite, not " + value);
		}
	}

	/** The current this one and another add up to. */
	Amperes plus(Amperes other)
	{
		return new Amperes(value + other.value);
	}

	/** The current left when another is taken from this one. */
	Amperes minus(Amperes other)
	{
		return new Amperes(value - other.value);
	}

	/** This current with its direction reversed. */
	Amperes negated()
	{
		return new Amperes(-value);
	}

	/** The potential this current develops across a resistance. */
	Volts times(Ohms resistance)
	{
		return new Volts(value * resistance.value());
	}

	/** The power this current carries at a potential. */
	Watts times(Volts potential)
	{
		return new Watts(value * potential.value());
	}

	/** The power this current dissipates in a resistance. */
	Watts dissipatedIn(Ohms resistance)
	{
		return new Watts(value * value * resistance.value());
	}
}
