package io.gifsync.septemcraft.api;

/** A power, signed by whether it is delivered or drawn. */
public record Watts(double value)
{
	/** No power at all. */
	public static final Watts ZERO = new Watts(0.0);

	/** Checks that a power is a number, which a power of either sign is. */
	public Watts
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A power is finite, not " + value);
		}
	}

	/** The current that carries this power at the given potential. */
	public Amperes over(Volts potential)
	{
		return new Amperes(value / potential.value());
	}

	/** The potential at which the given current carries this power. */
	public Volts over(Amperes current)
	{
		return new Volts(value / current.value());
	}

	/** What is left of this power once the given share of it is lost. */
	public Watts lessLoss(LossFraction loss)
	{
		return new Watts(value * loss.remainder());
	}
}
