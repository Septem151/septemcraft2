package io.gifsync.septemcraft.api;

/** What became of an attempt to solve a circuit. */
public enum SolutionStatus
{
	/** Every reading in the solution is the circuit's own answer. */
	SOLVED,

	/** Nothing drives the circuit, so every reading is zero. */
	DE_ENERGISED,

	/** The circuit contradicts itself and has no one answer, so every reading is zero. */
	SINGULAR,

	/** Something drives a bolted path back to itself, so no finite current satisfies it. */
	SHORTED,

	/** The circuit holds more nodes than the solver is sized for, so every reading is zero. */
	TOO_LARGE
}
