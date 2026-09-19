package io.gifsync.septemcraft.api;

/** How many turns a transformer's secondary carries for every turn of its primary. */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
@SuppressWarnings("DoNotCallSuggester")
public record TurnsRatio(double ratio)
{
	/** A winding that changes nothing. */
	public static final TurnsRatio UNITY = new TurnsRatio(1.0);

	/** The same transformer read from its other side. */
	public TurnsRatio inverted()
	{
		throw new UnsupportedOperationException("TurnsRatio.inverted() is not implemented.");
	}

	/** The potential this ratio turns the given one into. */
	public Volts applyTo(Volts potential)
	{
		throw new UnsupportedOperationException("TurnsRatio.applyTo(Volts) is not implemented.");
	}
}
