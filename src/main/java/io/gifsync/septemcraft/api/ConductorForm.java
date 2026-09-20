package io.gifsync.septemcraft.api;

/** What a conductor is built as, which is what decides how much it resists per block of run. */
public enum ConductorForm
{
	/** Wire strung between poles, cheap to run far and the more resistive of the two. */
	CATENARY_WIRE,

	/** Solid bar laid block by block, costly to run far and the less resistive of the two. */
	BUSBAR;

	/** What one block of this form resists, which is more for a wire than for a bar. */
	public Ohms perBlock()
	{
		return switch (this)
		{
			case CATENARY_WIRE -> ElectricalConstants.CATENARY_WIRE_OHMS_PER_BLOCK;
			case BUSBAR -> ElectricalConstants.BUSBAR_OHMS_PER_BLOCK;
		};
	}

	/** What a run of this form resists over the given length, which is its length times {@link #perBlock()}. */
	public Ohms resistanceOver(Blocks length)
	{
		return perBlock().times(length);
	}
}
