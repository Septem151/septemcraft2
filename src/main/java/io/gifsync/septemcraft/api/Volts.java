package io.gifsync.septemcraft.api;

/** An electrical potential, or a difference between two of them. */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
// TODO: Remove once implemented.
@SuppressWarnings("DoNotCallSuggester")
public record Volts(double value)
{
	/** No potential at all, which is what an unpowered node reads. */
	public static final Volts ZERO = new Volts(0.0);

	/** The current this potential drives through a resistance. */
	public Amperes over(Ohms resistance)
	{
		throw new UnsupportedOperationException("Volts.over(Ohms) is not implemented.");
	}

	/** The resistance that would draw the given current at this potential. */
	public Ohms over(Amperes current)
	{
		throw new UnsupportedOperationException("Volts.over(Amperes) is not implemented.");
	}

	/** The power carried by the given current at this potential. */
	public Watts times(Amperes current)
	{
		throw new UnsupportedOperationException("Volts.times(Amperes) is not implemented.");
	}
}
