package io.gifsync.septemcraft.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Solves a circuit for the potential at every node and the current through every element. A circuit
 * holding loads that answer their potential has no closed answer, so the solver works towards one;
 * where such a circuit has more than one answer, the stable one is the one it must find.
 *
 * @param maxNodes the most electrical points this solver will take on, beyond which a circuit reads
 *            as too large. Solving costs the cube of this, so it is what keeps a solve inside a
 *            tick, and a smaller one is a smaller ceiling rather than a slower solver. Blocks
 *            bolted to blocks collapse into one point before this is counted, so a run far longer
 *            than this still solves.
 */
public record CircuitSolver(int maxNodes)
{
	/** Checks that a solver is sized for a circuit at all. */
	public CircuitSolver
	{
		if (maxNodes < 1)
		{
			throw new IllegalArgumentException("A solver takes on at least one point, not " + maxNodes);
		}
	}

	/** A solver sized for the largest circuit the mod is expected to hand it. */
	public CircuitSolver()
	{
		this(ElectricalConstants.MAX_NODES_PER_CIRCUIT);
	}

	/** Solves a circuit from whatever the solver's own starting point is. */
	public Solution solve(Circuit circuit)
	{
		return solveFrom(circuit, Map.of());
	}

	/**
	 * Solves a circuit starting from the potentials given, which stands in for the answer a
	 * previous tick left behind.
	 */
	public Solution solveFrom(Circuit circuit, Map<NodeId, Volts> seed)
	{
		NodalAnalysis analysis = new NodalAnalysis(circuit);
		if (analysis.points() > maxNodes)
		{
			return nothing(SolutionStatus.TOO_LARGE);
		}

		if (circuit.sources().stream().noneMatch(Source::isRunning))
		{
			return nothing(SolutionStatus.DE_ENERGISED);
		}

		if (isShorted(circuit, analysis))
		{
			return nothing(SolutionStatus.SHORTED);
		}

		Map<NodeId, Volts> guesses = new HashMap<>(seed);
		Optional<CircuitReadings> first = analysis.readingsAt(guesses);
		if (first.isEmpty())
		{
			return nothing(SolutionStatus.SINGULAR);
		}

		CircuitReadings held = first.get();
		for (int pass = 1; pass <= ElectricalConstants.MAX_ITERATIONS; pass++)
		{
			step(guesses, held, circuit);
			Optional<CircuitReadings> taken = analysis.readingsAt(guesses);
			if (taken.isEmpty())
			{
				return nothing(SolutionStatus.SINGULAR);
			}

			CircuitReadings fresh = taken.get();
			if (hasSettled(circuit, held, fresh))
			{
				return fresh;
			}

			held = fresh;
		}

		return new CircuitReadings(SolutionStatus.UNSETTLED, held.voltages(), held.currents());
	}

	/**
	 * Whether a machine that sags not at all has a path resisting nothing from one of its own
	 * terminals back to the other. Such a machine is asked to hold a potential across a path that
	 * permits none, and no finite current answers that.
	 */
	private static boolean isShorted(Circuit circuit, NodalAnalysis analysis)
	{
		for (Source source : circuit.sources())
		{
			if (source.isRunning() && source.internalResistance().isNone()
				&& analysis.joins(source.from(), source.to()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Moves every potential the next pass is written around part of the way towards what the last one
	 * read. Stepping all the way lets a device that answers its own supply overshoot and answer the
	 * overshoot; stepping part of the way is what has it settle instead.
	 */
	private static void step(Map<NodeId, Volts> guesses, CircuitReadings taken, Circuit circuit)
	{
		for (NodeId node : circuit.nodes())
		{
			double guessed = guesses.getOrDefault(node, Volts.ZERO).value();
			double read = taken.voltageAt(node).value();
			guesses.put(node, new Volts(guessed + ElectricalConstants.RELAXATION_FACTOR * (read - guessed)));
		}
	}

	/** Whether two passes agree closely enough everywhere that the circuit counts as settled. */
	private static boolean hasSettled(Circuit circuit, CircuitReadings held, CircuitReadings fresh)
	{
		for (NodeId node : circuit.nodes())
		{
			double moved = Math.abs(fresh.voltageAt(node).value() - held.voltageAt(node).value());
			if (moved >= ElectricalConstants.CONVERGENCE_TOLERANCE)
			{
				return false;
			}
		}

		return true;
	}

	/** A circuit reading as nothing anywhere, which is what one that was not solved reads as. */
	private static Solution nothing(SolutionStatus status)
	{
		return new CircuitReadings(status, Map.of(), Map.of());
	}
}
