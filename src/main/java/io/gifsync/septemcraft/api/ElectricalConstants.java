package io.gifsync.septemcraft.api;

/**
 * Every number the electrical model turns on, and where each one comes from. Nothing else in this
 * package holds a numeric literal but 0, 1 and 2, so a value is changed here or nowhere.
 */
final class ElectricalConstants
{
	/**
	 * Volts a shaft drives per revolution a minute it turns. Settled at one: it is what makes a volt
	 * and an RPM the same number, so Create's 256 RPM ceiling is a 256 V ceiling and every volt above
	 * it comes from a transformer.
	 */
	static final double VOLTS_PER_RPM = 1.0;

	/**
	 * What a block of catenary wire resists. Provisional: no wire gauge exists, so a form has one
	 * figure. This is the long-haul figure the transmission fixtures are written against.
	 */
	static final Ohms CATENARY_WIRE_OHMS_PER_BLOCK = new Ohms(0.01);

	/**
	 * What a block of busbar resists. Provisional, and a fifth of catenary wire because a busbar is
	 * the high-current spine of a plant room and is sized for it.
	 */
	static final Ohms BUSBAR_OHMS_PER_BLOCK = new Ohms(0.002);

	/**
	 * How small a pivot may be before elimination calls a system singular. Provisional: every
	 * conductance the model stamps is many orders above it, so a pivot this small is a circuit that
	 * does not determine its own answer rather than arithmetic wearing out.
	 */
	static final double SINGULARITY_THRESHOLD = 1.0e-12;

	/**
	 * How far a potential may move between passes and still count as settled. Provisional, and well
	 * below a volt so a reading never wanders in its last displayed digit.
	 */
	static final double CONVERGENCE_TOLERANCE = 1.0e-9;

	/**
	 * How much of the way towards a fresh answer each pass steps. Provisional: below one it damps,
	 * which is what keeps a device holding its power from overshooting rather than settling.
	 */
	static final double RELAXATION_FACTOR = 0.75;

	/**
	 * How many passes a circuit gets to settle before the last one is held instead. Provisional, and
	 * generous: an oscillating grid must not become an oscillating world.
	 */
	static final int MAX_ITERATIONS = 100;

	/**
	 * How many electrical points a circuit may carry. Provisional, and the ceiling the dense solver is
	 * chosen under - elimination costs the cube of this, so it is what keeps a solve inside a tick.
	 */
	static final int MAX_NODES_PER_CIRCUIT = 256;

	private ElectricalConstants()
	{
	}
}
