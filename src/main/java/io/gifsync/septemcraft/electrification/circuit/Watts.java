package io.gifsync.septemcraft.electrification.circuit;

/** A power, of either sign - a negative power is power flowing back the way it came. */
record Watts(double value)
{
	/** No power at all, which is what an idle machine moves. */
	static final Watts ZERO = new Watts(0.0);

	/** Checks that a power is a number, which a power of either sign is. */
	Watts
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A power is finite, not " + value);
		}
	}

	/** The power this one and another add up to. */
	Watts plus(Watts other)
	{
		return new Watts(value + other.value);
	}

	/** The power left when another is taken from this one. */
	Watts minus(Watts other)
	{
		return new Watts(value - other.value);
	}

	/** This power with its direction reversed. */
	Watts negated()
	{
		return new Watts(-value);
	}

	/** The current this power takes at a potential. */
	Amperes over(Volts potential)
	{
		return new Amperes(value / potential.value());
	}

	/** The potential this power implies at a current. */
	Volts over(Amperes current)
	{
		return new Volts(value / current.value());
	}

	/** The fraction of this power a transformer keeps when it takes its loss. */
	Watts lessLoss(LossFraction loss)
	{
		return new Watts(value * loss.remainder());
	}
}
