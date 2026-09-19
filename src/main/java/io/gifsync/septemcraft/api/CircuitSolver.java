package io.gifsync.septemcraft.api;

import java.util.Map;

/**
 * Solves a circuit for the potential at every node and the current through every element. A circuit
 * holding loads that answer their potential has no closed answer, so the solver works towards one;
 * where such a circuit has more than one answer, the stable one is the one it must find.
 */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
@SuppressWarnings("DoNotCallSuggester")
public final class CircuitSolver
{
	/** The most nodes this solver will take on, beyond which a circuit reads as too large. */
	public int maxNodes()
	{
		throw new UnsupportedOperationException("CircuitSolver.maxNodes() is not implemented.");
	}

	/** Solves a circuit from whatever the solver's own starting point is. */
	public Solution solve(Circuit circuit)
	{
		throw new UnsupportedOperationException("CircuitSolver.solve(Circuit) is not implemented.");
	}

	/**
	 * Solves a circuit starting from the potentials given, which stands in for the answer a
	 * previous tick left behind. The answer reached must not depend on where it started.
	 */
	public Solution solveFrom(Circuit circuit, Map<NodeId, Volts> seed)
	{
		throw new UnsupportedOperationException("CircuitSolver.solveFrom(..) is not implemented.");
	}
}
