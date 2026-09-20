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
 *
 * <p>Every one of those three figures is a size and not a direction. A device answers how much it
 * is given rather than which way round, so a potential the other way runs it just the same and it
 * draws the other way in answer. A shaft turning backwards drives its line backwards, and what
 * hangs off that line works.
 */
public record Load(ElementId id, NodeId from, NodeId to, LoadClass behaviour, Watts ratedPower,
	Volts ratedVoltage, Volts minimumVoltage) implements Element
{
	/** Checks that a device is across two nodes and that its nameplate names a device at all. */
	public Load
	{
		if (from.equals(to))
		{
			throw new IllegalArgumentException("A device is across two nodes, not " + from + " and itself");
		}

		if (!(ratedPower.value() > 0.0))
		{
			throw new IllegalArgumentException("A device is rated for a power, and " + ratedPower + " names none");
		}

		if (!(ratedVoltage.value() > 0.0))
		{
			throw new IllegalArgumentException(
				"A device is rated at a potential, and " + ratedVoltage + " names none");
		}

		if (minimumVoltage.value() < 0.0)
		{
			throw new IllegalArgumentException("A device gives up at a potential, not at " + minimumVoltage);
		}

		if (minimumVoltage.value() > ratedVoltage.value())
		{
			throw new IllegalArgumentException("A device giving up at " + minimumVoltage
				+ " would never be running at the " + ratedVoltage + " it is measured at");
		}
	}

	/**
	 * The current this load draws at the potential it is rated for, which is its rated power over
	 * its rated voltage. Every class draws that much at that one potential, and the classes differ
	 * only either side of it.
	 */
	public Amperes ratedCurrent()
	{
		return ratedPower.over(ratedVoltage);
	}

	/**
	 * The current this load draws when given the potential named. Once that potential reaches its
	 * minimum, which either way round it does at the same size, what the device draws is what its
	 * class decides; below it the device is not running and draws nothing at all, which is a
	 * step-down to none and not a taper towards it.
	 */
	public Amperes drawAt(Volts potential)
	{
		if (isStalledAt(potential))
		{
			return Amperes.ZERO;
		}

		return switch (behaviour)
		{
			case CONSTANT_RESISTANCE -> potential.over(resistance());
			case CONSTANT_CURRENT -> holdingItsCurrentAt(potential);
			case CONSTANT_POWER -> holdingItsPowerAt(potential);
		};
	}

	/**
	 * Whether this device is stalled at the potential named; once that potential is as large as its
	 * minimum it is running. How large is the whole of the question, a device being given the same
	 * potential whichever way the line it hangs off runs. What it draws while it runs is its
	 * class's to say, and a device that is stalled draws no power.
	 */
	boolean isStalledAt(Volts potential)
	{
		return !(Math.abs(potential.value()) >= minimumVoltage.value());
	}

	/** The resistance this load presents at the potential it is rated for. */
	Ohms resistance()
	{
		return ratedVoltage.over(ratedCurrent());
	}

	/**
	 * The current this load draws holding its current at a potential, which is its rated current
	 * running the way that potential runs. Holding the current means holding how much of it there
	 * is; a device pushing current into a line that is driving it backwards would be a machine, not
	 * a device.
	 */
	private Amperes holdingItsCurrentAt(Volts potential)
	{
		double current = ratedCurrent().value();

		return new Amperes(potential.value() < 0.0 ? -current : current);
	}

	/**
	 * The current this load draws holding its power at a potential. A potential it cannot be divided
	 * by is not one the device runs on, however far above a floor of none it sits.
	 */
	private Amperes holdingItsPowerAt(Volts potential)
	{
		double current = ratedPower.value() / potential.value();

		return Double.isFinite(current) ? new Amperes(current) : Amperes.ZERO;
	}
}
