package io.gifsync.septemcraft.electrification.circuit;

/** The three ways a device's draw answers to the voltage it is given. */
enum LoadClass
{
	/** Draws what its resistance allows, so its power follows the square of its voltage. */
	CONSTANT_RESISTANCE,

	/** Draws the same current whatever it is given, so its power follows its voltage. */
	CONSTANT_CURRENT,

	/** Draws the same power whatever it is given, so its current rises as its voltage sags. */
	CONSTANT_POWER
}
