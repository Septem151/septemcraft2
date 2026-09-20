package io.gifsync.septemcraft.api;

/**
 * Something drawing from the circuit. Three figures describe one: its nameplate is the power it
 * draws at the potential it is rated for, its class is what it does when given some other
 * potential, and its minimum is the potential below which it does not run at all.
 *
 * <p>The nameplate and the minimum are separate numbers because they answer separate questions. A
 * device rated {@code 1,024 W @ 128 V} is measured at that potential and still runs on a line that
 * has sagged below it; where it gives up is its own figure, and how far a line may sag before it
 * does is what sizing a run is about. A minimum of {@link Volts#ZERO} is a device with no floor,
 * which runs on whatever it is given.
 */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
// TODO: Remove once implemented.
@SuppressWarnings("DoNotCallSuggester")
public record Load(ElementId id, NodeId from, NodeId to, LoadClass behaviour, Watts ratedPower,
	Volts ratedVoltage, Volts minimumVoltage) implements Element
{
	/**
	 * The current this load draws at the potential it is rated for, which is its rated power over
	 * its rated voltage. Every class draws that much at that one potential, and the classes differ
	 * only either side of it.
	 */
	public Amperes ratedCurrent()
	{
		throw new UnsupportedOperationException("Load.ratedCurrent() is not implemented.");
	}

	/**
	 * The current this load draws when given the potential named. At or above its minimum that is
	 * what its class decides; below it the device is not running and draws nothing at all, which is
	 * a step-down to none and not a taper towards it.
	 */
	public Amperes drawAt(Volts potential)
	{
		throw new UnsupportedOperationException("Load.drawAt(Volts) is not implemented.");
	}
}
