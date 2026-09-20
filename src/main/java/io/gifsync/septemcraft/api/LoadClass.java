package io.gifsync.septemcraft.api;

/** How a load answers a change in the potential it is given. */
public enum LoadClass
{
	/**
	 * Draws by its own resistance, so halving its potential halves its current and quarters its
	 * power. A bare resistor is one, and so is anything whose stress impact rises with the speed it
	 * is turning at.
	 */
	CONSTANT_RESISTANCE,

	/**
	 * Draws the same current whatever it is given, so its power follows its potential. A motor
	 * turning a Create machine is one: that machine's impact is a fixed su/RPM and one ampere is
	 * one su/RPM, so a sagging bus turns the motor slower without changing what it pulls.
	 */
	CONSTANT_CURRENT,

	/**
	 * Draws the same power whatever it is given, so halving its potential doubles its current.
	 * Anything holding its own draw against a supply that moves is one, such as a battery taking a
	 * charge at a set rate. It is also the class no circuit answers in one pass of arithmetic,
	 * since what it draws depends on the potential the draw itself decides.
	 */
	CONSTANT_POWER;

	/**
	 * Whether a device of this class is written onto a solve as a resistance, which the solve then
	 * answers exactly, rather than as the current it was found to be drawing. Only a device drawing
	 * by its own resistance is the former.
	 */
	boolean stampsAsResistance()
	{
		return this == CONSTANT_RESISTANCE;
	}
}
