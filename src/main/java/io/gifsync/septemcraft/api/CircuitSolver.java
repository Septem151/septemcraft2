package io.gifsync.septemcraft.api;

import java.util.Map;

/**
 * Solves a circuit for the potential at every node and the current through every element. A circuit
 * holding loads that answer their potential has no closed answer, so the solver works towards one;
 * where such a circuit has more than one answer, the stable one is the one it must find.
 */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
// TODO: Remove once implemented.
@SuppressWarnings("DoNotCallSuggester")
public final class CircuitSolver
{
	private final int maxNodes;

	/** A solver sized for the largest circuit the mod is expected to hand it. */
	public CircuitSolver()
	{
		this(defaultMaxNodes());
	}

	/**
	 * A solver sized for the number of nodes named. Solving costs the cube of this, so it is what
	 * keeps a solve inside a tick, and a smaller one is a smaller ceiling rather than a slower
	 * solver.
	 */
	public CircuitSolver(int maxNodes)
	{
		this.maxNodes = maxNodes;
	}

	/**
	 * The most electrical points this solver will take on, beyond which a circuit reads as too
	 * large. Blocks bolted to blocks collapse into one point before this is counted, so a run far
	 * longer than this still solves.
	 */
	public int maxNodes()
	{
		return maxNodes;
	}

	private static int defaultMaxNodes()
	{
		throw new UnsupportedOperationException("CircuitSolver() is not implemented.");
	}

	/** Solves a circuit from whatever the solver's own starting point is. */
	public Solution solve(Circuit circuit)
	{
		throw new UnsupportedOperationException("CircuitSolver.solve(Circuit) is not implemented.");
	}

	/**
	 * Solves a circuit starting from the potentials given, which stands in for the answer a
	 * previous tick left behind.
	 */
	public Solution solveFrom(Circuit circuit, Map<NodeId, Volts> seed)
	{
		throw new UnsupportedOperationException("CircuitSolver.solveFrom(..) is not implemented.");
	}
}
