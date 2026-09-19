package io.gifsync.septemcraft.electrification.circuit;

/** A resistance, of either sign. */
record Ohms(double value)
{
	/** No resistance at all, which is what two things bolted together present. */
	static final Ohms ZERO = new Ohms(0.0);

	/** Checks that a resistance is a number, which a resistance of either sign is. */
	Ohms
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A resistance is finite, not " + value);
		}
	}

	/** The resistance this one and another present in series. */
	Ohms plus(Ohms other)
	{
		return new Ohms(value + other.value);
	}

	/** This resistance repeated over a run of conductor. */
	Ohms over(Blocks run)
	{
		return new Ohms(value * run.count());
	}

	/** Whether this resistance is none at all, which is what a contracted joint presents. */
	boolean isNone()
	{
		return value == 0.0;
	}
}
