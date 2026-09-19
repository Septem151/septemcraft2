package io.gifsync.septemcraft.electrification.circuit;

/** How a solve ended, which is a fact about the circuit rather than about the arithmetic. */
enum SolutionStatus
{
	/** The circuit settled, and every reading is the answer it settled on. */
	SOLVED,

	/** Nothing on the circuit was turning, so every reading is nothing. */
	DE_ENERGISED,

	/** The circuit does not determine its own answer, so it reads as dead rather than as nonsense. */
	SINGULAR,

	/** The circuit was still moving when the iterations ran out, so the last settled state is held. */
	NOT_CONVERGED,

	/** The circuit has more nodes than the solver is sized for, so it reads as dead. */
	TOO_LARGE
}
