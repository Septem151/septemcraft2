package io.gifsync.septemcraft.api;

/** What became of an attempt to solve a circuit. */
public enum SolutionStatus
{
	/** Every reading in the solution is the circuit's own answer. */
	SOLVED,

	/** Nothing drives the circuit, so every reading is zero. */
	DE_ENERGISED,

	/**
	 * A run of conductor resisting nothing joins the two terminals of a machine that sags not at
	 * all. Such a machine is then asked to hold a potential across a path that permits none, and no
	 * finite current answers that. Every reading is zero.
	 *
	 * <p>This is the fault a player has built and can go and find. A machine whose own windings
	 * resist anything at all is not this, however hard it is shorted: the windings hold the current
	 * to a large figure and a real one.
	 */
	SHORTED,

	/**
	 * The circuit does not determine its own answer for some other reason, such as two machines
	 * that sag not at all being wired in parallel and disagreeing about the potential. Every
	 * reading is zero.
	 */
	SINGULAR,

	/** The circuit holds more nodes than the solver is sized for, so every reading is zero. */
	TOO_LARGE
}
