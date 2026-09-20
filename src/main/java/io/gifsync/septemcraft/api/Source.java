package io.gifsync.septemcraft.api;

/**
 * Something driving the circuit, which raises {@link #to()} above {@link #from()} by its
 * electromotive force. That force is what it would hold across its terminals driving nothing; its
 * internal resistance is what its own windings take out of that under load, so what reaches the
 * circuit falls as more is drawn from it.
 *
 * <p>Oriented this way a machine that is driving reads a positive current and a machine the bus is
 * driving instead reads a negative one, which is the same convention every other element keeps.
 *
 * <p>An internal resistance of {@link Ohms#ZERO} is a machine that sags not at all, which no real
 * one does. It is the only thing a circuit can hold that a short across makes unanswerable, and
 * {@link SolutionStatus#SHORTED} is that case.
 */
public record Source(ElementId id, NodeId from, NodeId to, Volts electromotiveForce,
	Ohms internalResistance) implements Element
{
	/** Checks that a machine is across two nodes and that its own windings take something or nothing. */
	public Source
	{
		if (from.equals(to))
		{
			throw new IllegalArgumentException("A machine is across two nodes, not " + from + " and itself");
		}

		if (internalResistance.value() < 0.0)
		{
			throw new IllegalArgumentException(
				"A machine's windings resist something or nothing, not " + internalResistance);
		}
	}

	/** Whether this machine is driving at all, which one holding no potential is not. */
	boolean isRunning()
	{
		return electromotiveForce.value() != 0.0;
	}
}
