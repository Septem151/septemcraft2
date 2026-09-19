package io.gifsync.septemcraft.electrification.circuit;

/** The three forms a conductor takes, and how much each resists over a block of run. */
enum ConductorForm
{
	/** Long-haul transmission strung between poles. */
	CATENARY_WIRE,

	/** Rigid high-current distribution inside a plant room. */
	BUSBAR,

	/** Thin jacketed conduit for interior runs. */
	SURFACE_WIRING;

	/** What one block of this form resists. */
	Ohms perBlock()
	{
		return switch (this)
		{
			case CATENARY_WIRE -> ElectricalConstants.CATENARY_WIRE_OHMS_PER_BLOCK;
			case BUSBAR -> ElectricalConstants.BUSBAR_OHMS_PER_BLOCK;
			case SURFACE_WIRING -> ElectricalConstants.SURFACE_WIRING_OHMS_PER_BLOCK;
		};
	}

	/** What a run of this form resists over its length. */
	Ohms resistanceOver(Blocks run)
	{
		return perBlock().over(run);
	}
}
