package io.gifsync.septemcraft.api;

/** A rotational speed, in revolutions per minute. */
public record Rpm(double value)
{
	/** A shaft at rest. */
	public static final Rpm ZERO = new Rpm(0.0);

	/** Checks that a speed is a number, which a speed either way round is. */
	public Rpm
	{
		if (!Double.isFinite(value))
		{
			throw new IllegalArgumentException("A speed is finite, not " + value);
		}
	}

	/**
	 * The potential a machine turning at this speed drives, which is one volt for every revolution
	 * a minute. A shaft turning the other way drives the same potential the other way.
	 */
	public Volts driving()
	{
		return new Volts(value * ElectricalConstants.VOLTS_PER_RPM);
	}
}
