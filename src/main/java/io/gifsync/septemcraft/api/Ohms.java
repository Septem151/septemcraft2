package io.gifsync.septemcraft.api;

/** A resistance. */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
@SuppressWarnings("DoNotCallSuggester")
public record Ohms(double value) implements Quantity
{
	/** No resistance at all, which is what a bolted joint between two blocks offers. */
	public static final Ohms ZERO = new Ohms(0.0);

	/** This resistance and another in series. */
	public Ohms plus(Ohms other)
	{
		throw new UnsupportedOperationException("Ohms.plus(Ohms) is not implemented.");
	}
}
