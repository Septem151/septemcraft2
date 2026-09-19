package io.gifsync.septemcraft.api;

/** How a load answers a change in the potential it is given. */
public enum LoadClass
{
	/** Draws by its own resistance, so halving its potential halves its current. */
	CONSTANT_RESISTANCE,

	/** Draws the same current whatever it is given. */
	CONSTANT_CURRENT,

	/** Draws the same power whatever it is given, so halving its potential doubles its current. */
	CONSTANT_POWER
}
