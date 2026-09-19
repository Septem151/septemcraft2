package io.gifsync.septemcraft.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks what a nameplate amounts to, and what each kind of device does when the bus it is on is
 * not at the potential it was rated for. A device that holds its power is what makes a circuit
 * answer itself rather than fall out of one pass of arithmetic, so what it draws as its supply
 * sags is the behaviour the solver is built around.
 */
class LoadTest
{
	private static final double EXACT = 1.0e-12;

	private static final NodeId LIVE = new NodeId(1);

	private static final NodeId RETURN = new NodeId(0);

	private static final Watts RATED_POWER = new Watts(1024.0);

	private static final Volts RATED_VOLTAGE = new Volts(128.0);

	/** A nameplate is a power at a potential, and the resistance and current follow from the pair. */
	@Test
	void aNameplateGivesAResistanceAndARatedCurrent()
	{
		Load load = rated(LoadClass.CONSTANT_RESISTANCE);

		assertEquals(16.0, load.resistance().value(), EXACT);
		assertEquals(8.0, load.ratedCurrent().value(), EXACT);
	}

	/**
	 * Given half what it was rated for, a device drawing by its resistance halves its current, one
	 * drawing a fixed current holds it, and one holding its power doubles it. Those three answers
	 * are the whole of what the class means.
	 */
	@ParameterizedTest
	@CsvSource({
		"CONSTANT_RESISTANCE, 128.0, 8.0",
		"CONSTANT_RESISTANCE,  64.0, 4.0",
		"CONSTANT_RESISTANCE, 256.0, 16.0",
		"CONSTANT_CURRENT,    128.0, 8.0",
		"CONSTANT_CURRENT,     64.0, 8.0",
		"CONSTANT_CURRENT,    256.0, 8.0",
		"CONSTANT_POWER,      128.0, 8.0",
		"CONSTANT_POWER,       64.0, 16.0",
		"CONSTANT_POWER,      256.0, 4.0"})
	void eachKindOfDeviceAnswersItsSupplyInItsOwnWay(LoadClass behaviour, double supplied, double expected)
	{
		assertEquals(expected, rated(behaviour).drawAt(new Volts(supplied)).value(), EXACT);
	}

	/**
	 * A bus with nothing driving it is an ordinary state, and a device holding its power would draw
	 * without limit on one. Asking for the current there must answer a figure, and the interesting
	 * case is not zero itself but a potential just off it, which is where dividing runs away.
	 */
	@ParameterizedTest
	@ValueSource(doubles = {0.0, -0.0, 1.0e-300, 1.0e-12, -1.0e-9, -64.0})
	void aDeviceHoldingItsPowerOnAnAllButDeadBusDrawsSomethingFinite(double supplied)
	{
		double drawn = rated(LoadClass.CONSTANT_POWER).drawAt(new Volts(supplied)).value();

		assertTrue(Double.isFinite(drawn), "a dead bus draws " + drawn);
	}

	/** A dead bus draws nothing at all, whatever the device on it is rated for. */
	@Test
	void aDeviceOnADeadBusDrawsNothing()
	{
		for (LoadClass behaviour : LoadClass.values())
		{
			assertEquals(0.0, rated(behaviour).drawAt(Volts.ZERO).value(), EXACT, behaviour + " draws off a dead bus");
		}
	}

	/** A nameplate naming no power, or no potential to take it at, names no device. */
	@Test
	void aNameplateNamingNoDeviceIsRefused()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, RATED_POWER, Volts.ZERO));
		assertThrows(IllegalArgumentException.class,
			() -> new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, Watts.ZERO, RATED_VOLTAGE));
		assertThrows(IllegalArgumentException.class,
			() -> new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, new Watts(-1024.0), RATED_VOLTAGE));
		assertThrows(IllegalArgumentException.class,
			() -> new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, RATED_POWER, new Volts(-128.0)));
	}

	/** A device wired to one node twice is across nothing, and cannot be asked what it draws. */
	@Test
	void aDeviceWiredToItselfIsRefused()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new Load(LIVE, LIVE, LoadClass.CONSTANT_POWER, RATED_POWER, RATED_VOLTAGE));
	}

	private static Load rated(LoadClass behaviour)
	{
		return new Load(LIVE, RETURN, behaviour, RATED_POWER, RATED_VOLTAGE);
	}
}
