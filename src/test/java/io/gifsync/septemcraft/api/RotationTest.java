package io.gifsync.septemcraft.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks the one figure the electrical side is fixed on: a machine drives one volt for every
 * revolution a minute its shaft turns.
 */
class RotationTest
{
	private static final double EXACT = 1.0e-12;

	/** Speed and potential are the same number, so a shaft's reading is its own. */
	@Test
	void aShaftDrivesAsManyVoltsAsItTurnsRevolutions()
	{
		assertEquals(128.0, new Rpm(128.0).driving().value(), EXACT);
		assertEquals(32.0, new Rpm(32.0).driving().value(), EXACT);
		assertEquals(256.0, new Rpm(256.0).driving().value(), EXACT);
	}

	/** A shaft turned backwards drives backwards, which is what lets a machine be driven by the bus. */
	@Test
	void aShaftTurnedTheOtherWayDrivesTheOtherWay()
	{
		assertEquals(-64.0, new Rpm(-64.0).driving().value(), EXACT);
	}

	/** A shaft at rest drives nothing. */
	@Test
	void aStoppedShaftDrivesNothing()
	{
		assertEquals(0.0, Rpm.ZERO.driving().value(), EXACT);
	}
}
