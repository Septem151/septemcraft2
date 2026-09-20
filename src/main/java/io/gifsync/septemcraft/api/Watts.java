package io.gifsync.septemcraft.api;

/** A power, signed by whether it is delivered or drawn. */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
// TODO: Remove once implemented.
@SuppressWarnings("DoNotCallSuggester")
public record Watts(double value)
{
	/** No power at all. */
	public static final Watts ZERO = new Watts(0.0);

	/** The current that carries this power at the given potential. */
	public Amperes over(Volts potential)
	{
		throw new UnsupportedOperationException("Watts.over(Volts) is not implemented.");
	}

	/** The potential at which the given current carries this power. */
	public Volts over(Amperes current)
	{
		throw new UnsupportedOperationException("Watts.over(Amperes) is not implemented.");
	}

	/** What is left of this power once the given share of it is lost. */
	public Watts lessLoss(LossFraction loss)
	{
		throw new UnsupportedOperationException("Watts.lessLoss(LossFraction) is not implemented.");
	}
}
