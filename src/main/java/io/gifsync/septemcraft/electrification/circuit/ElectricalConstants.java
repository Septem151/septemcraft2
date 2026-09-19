package io.gifsync.septemcraft.electrification.circuit;

/**
 * Every number the electrical model turns on, and where each one comes from. Nothing else in the
 * electrical packages holds a numeric literal but 0, 1 and 2, so a value is changed here or nowhere.
 */
final class ElectricalConstants
{
	/**
	 * Volts a shaft produces per RPM it turns at. Settled at one: it is what makes a volt and an RPM
	 * the same number, so Create's 256 RPM ceiling is a 256 V ceiling and every volt above it comes
	 * from a transformer.
	 */
	static final double VOLTS_PER_RPM = 1.0;

	/**
	 * Watts a stress unit buys. Settled at one: watts and total stress are one currency, so power is
	 * conserved across a generator and there is no second economy to balance. This is the one lever
	 * between the two, and moving it moves everything.
	 */
	static final double WATTS_PER_STRESS_UNIT = 1.0;

	/**
	 * What a block of catenary wire resists. Provisional: no wire gauge exists, so a form has one
	 * figure. This one is the long-haul figure the transmission fixtures are written against.
	 */
	static final Ohms CATENARY_WIRE_OHMS_PER_BLOCK = new Ohms(0.01);

	/**
	 * What a block of busbar resists. Provisional, and a fifth of catenary wire because a busbar is
	 * the high-current spine of a plant room and is sized for it.
	 */
	static final Ohms BUSBAR_OHMS_PER_BLOCK = new Ohms(0.002);

	/**
	 * What a block of surface wiring resists. Provisional and invented - no fixture pins it. It is
	 * the thinnest of the three forms, so it resists the most.
	 */
	static final Ohms SURFACE_WIRING_OHMS_PER_BLOCK = new Ohms(0.05);

	/**
	 * What a generator's own windings resist. Provisional: it sets how stiffly machines in parallel
	 * share a load, and so how far a gearing mismatch goes before one machine motors another.
	 */
	static final Ohms WINDING_RESISTANCE = new Ohms(0.5);

	/**
	 * What a transformer loses of the power crossing it. Provisional, and zero until the loss is
	 * decided, so the element carries a loss it is handed rather than one it assumes.
	 */
	static final LossFraction TRANSFORMER_LOSS = new LossFraction(0.0);

	/**
	 * How small a pivot may be before elimination calls a system singular. Provisional: every
	 * conductance the model stamps is many orders above it, so a pivot this small is a circuit that
	 * does not determine its own answer rather than arithmetic wearing out.
	 */
	static final double SINGULARITY_THRESHOLD = 1.0e-12;

	/**
	 * How far a node's voltage may move between iterations and still count as settled. Provisional,
	 * and well below a volt so a reading never wanders in its last displayed digit.
	 */
	static final double CONVERGENCE_TOLERANCE = 1.0e-9;

	/**
	 * How much of the way towards a fresh answer each iteration steps. Provisional: below one it
	 * damps, which is what keeps a constant-power load from oscillating rather than settling.
	 */
	static final double RELAXATION_FACTOR = 0.75;

	/**
	 * How many iterations a circuit gets to settle before the last settled state is held instead.
	 * Provisional, and generous: an oscillating grid must not become an oscillating world.
	 */
	static final int MAX_ITERATIONS = 100;

	/**
	 * How many nodes a circuit may carry. Provisional, and the ceiling the dense solver is chosen
	 * under - elimination costs the cube of this, so it is what keeps a solve inside a tick.
	 */
	static final int MAX_NODES_PER_CIRCUIT = 256;

	private ElectricalConstants()
	{
	}
}
