package io.gifsync.septemcraft.api;

/** What a conductor is built as, which is what decides how much it resists per block of run. */
// Every method here throws until the solver is written, which is what the tests beside this
// package are for. @DoNotCall is not the answer: it would stop those tests compiling.
@SuppressWarnings("DoNotCallSuggester")
public enum ConductorForm
{
	/** Wire strung between poles, cheap to run far and the more resistive of the two. */
	CATENARY_WIRE,

	/** Solid bar laid block by block, costly to run far and the less resistive of the two. */
	BUSBAR;

	/** What one block of this form resists. */
	public Ohms perBlock()
	{
		throw new UnsupportedOperationException("ConductorForm.perBlock() is not implemented.");
	}

	/** What a run of this form resists over the given length. */
	public Ohms resistanceOver(Blocks length)
	{
		throw new UnsupportedOperationException("ConductorForm.resistanceOver(Blocks) is not implemented.");
	}
}
