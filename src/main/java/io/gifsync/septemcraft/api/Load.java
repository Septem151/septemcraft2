package io.gifsync.septemcraft.api;

/**
 * Something drawing from the circuit. Its nameplate is the power it draws at the potential it is
 * rated for; its class is what it does when given some other potential.
 */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
@SuppressWarnings("DoNotCallSuggester")
public record Load(NodeId from, NodeId to, LoadClass behaviour, Watts ratedPower, Volts ratedVoltage)
	implements
		Element
{
	/** The resistance this nameplate amounts to at the potential it is rated for. */
	public Ohms resistance()
	{
		throw new UnsupportedOperationException("Load.resistance() is not implemented.");
	}

	/** The current this nameplate draws at the potential it is rated for. */
	public Amperes ratedCurrent()
	{
		throw new UnsupportedOperationException("Load.ratedCurrent() is not implemented.");
	}

	/** The current this load draws when given the potential named, which its class decides. */
	public Amperes drawAt(Volts potential)
	{
		throw new UnsupportedOperationException("Load.drawAt(Volts) is not implemented.");
	}
}
