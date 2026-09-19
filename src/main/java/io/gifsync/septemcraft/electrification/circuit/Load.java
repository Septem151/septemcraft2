package io.gifsync.septemcraft.electrification.circuit;

/**
 * A device drawing power between two nodes, rated the way a nameplate rates one - so many watts at
 * so many volts - and behaving at other voltages the way its class says. Its current is positive
 * when it flows from the first node to the second, which is the direction that consumes.
 */
record Load(NodeId from, NodeId to, LoadClass loadClass, Watts ratedPower, Volts ratedVoltage)
{
	/** Checks that a load sits between two nodes and carries a nameplate its class can be read off. */
	Load
	{
		if (from.equals(to))
		{
			throw new IllegalArgumentException("A load sits between two nodes, not " + from + " and itself");
		}

		if (ratedVoltage.value() == 0.0)
		{
			throw new IllegalArgumentException("A load is rated at a voltage, and zero volts names no device");
		}

		if (ratedPower.value() == 0.0)
		{
			throw new IllegalArgumentException("A load is rated at a power, and zero watts names no device");
		}
	}

	/**
	 * The resistance this load presents, which is its whole behaviour when it is a constant-resistance
	 * one and its resistance at its rating when it is not.
	 */
	Ohms resistance()
	{
		return ratedVoltage.over(ratedCurrent());
	}

	/** The current this load draws at its rating, which is what a constant-current one draws always. */
	Amperes ratedCurrent()
	{
		return ratedPower.over(ratedVoltage);
	}

	/**
	 * The current this load draws with a given potential across it. A constant-power load given
	 * nothing draws nothing: below its rating a device does not run, and it cannot take infinite
	 * current from a dead circuit to prove it.
	 */
	Amperes drawAt(Volts terminal)
	{
		return switch (loadClass)
		{
			case CONSTANT_RESISTANCE -> terminal.over(resistance());
			case CONSTANT_CURRENT -> ratedCurrent();
			case CONSTANT_POWER -> terminal.value() == 0.0 ? Amperes.ZERO : ratedPower.over(terminal);
		};
	}
}
