package io.gifsync.septemcraft.api;

/** A rotational speed, in revolutions per minute. */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
// TODO: Remove once implemented.
@SuppressWarnings("DoNotCallSuggester")
public record Rpm(double value)
{
	/** A shaft at rest. */
	public static final Rpm ZERO = new Rpm(0.0);

	/**
	 * The potential a machine turning at this speed drives, which is one volt for every revolution
	 * a minute. A shaft turning the other way drives the same potential the other way.
	 */
	public Volts driving()
	{
		throw new UnsupportedOperationException("Rpm.driving() is not implemented.");
	}
}
