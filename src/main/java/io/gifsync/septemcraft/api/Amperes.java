package io.gifsync.septemcraft.api;

/** A current, signed by the direction it runs in. */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
// TODO: Remove once implemented.
@SuppressWarnings("DoNotCallSuggester")
public record Amperes(double value)
{
	/** No current at all. */
	public static final Amperes ZERO = new Amperes(0.0);

	/** The potential this current raises across a resistance. */
	public Volts times(Ohms resistance)
	{
		throw new UnsupportedOperationException("Amperes.times(Ohms) is not implemented.");
	}

	/** The power this current carries at the given potential. */
	public Watts times(Volts potential)
	{
		throw new UnsupportedOperationException("Amperes.times(Volts) is not implemented.");
	}

	/** The power this current burns in a resistance, which it does whichever way it runs. */
	public Watts dissipatedIn(Ohms resistance)
	{
		throw new UnsupportedOperationException("Amperes.dissipatedIn(Ohms) is not implemented.");
	}
}
