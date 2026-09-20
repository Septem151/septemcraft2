package io.gifsync.septemcraft.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks what a nameplate amounts to, what each kind of device does when the bus it is on is not at
 * the potential it was rated for, and where it stops doing anything at all. A device that holds its
 * power is what makes a circuit answer itself rather than fall out of one pass of arithmetic, and
 * the floor beneath it is what stops that answer running away.
 */
class LoadTest
{
	/**
	 * How far a drawn figure may stray before it is the wrong figure. Every number here is one
	 * division rather than the end of a solution, so there is nothing to accumulate and this is tight.
	 */
	private static final double EXACT = 1.0e-12;

	private static final ElementId ID = new ElementId(0);

	private static final NodeId LIVE = new NodeId(1);

	private static final NodeId RETURN = new NodeId(0);

	private static final Watts RATED_POWER = new Watts(1024.0);

	private static final Volts RATED_VOLTAGE = new Volts(128.0);

	/** Where the devices here give up, which is a quarter of what they are rated for. */
	private static final Volts MINIMUM = new Volts(32.0);

	/**
	 * A nameplate is a power at a potential, and the current follows from the pair. Every class
	 * draws that much at the potential it was rated for, whatever each does either side of it.
	 */
	@Test
	void everyKindOfDeviceDrawsItsRatedCurrentAtItsRatedPotential()
	{
		for (LoadClass behaviour : LoadClass.values())
		{
			Load load = rated(behaviour);

			assertEquals(8.0, load.ratedCurrent().value(), EXACT, behaviour + " is rated for the wrong current");
			assertEquals(load.ratedCurrent().value(), load.drawAt(RATED_VOLTAGE).value(), EXACT,
				behaviour + " does not draw its rated current at its rated potential");
		}
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
	 * A device given less than it needs is not running, and a device that is not running draws
	 * no amps. This is what bounds a device holding its power: the harder it would pull as its
	 * supply sagged, the sooner it stops pulling at all.
	 */
	@Test
	void aDeviceGivenLessThanItNeedsDrawsNothing()
	{
		for (LoadClass behaviour : LoadClass.values())
		{
			Load load = rated(behaviour);

			assertEquals(0.0, load.drawAt(new Volts(MINIMUM.value() - 1.0)).value(), EXACT,
				behaviour + " runs below its minimum");
			assertEquals(0.0, load.drawAt(new Volts(1.0)).value(), EXACT, behaviour + " runs on almost nothing");
			assertEquals(0.0, load.drawAt(Volts.ZERO).value(), EXACT, behaviour + " runs on a dead bus");
		}
	}

	/** A device given exactly what it needs is running, and draws whatever its class says it does. */
	@Test
	void aDeviceGivenExactlyWhatItNeedsIsRunning()
	{
		for (LoadClass behaviour : LoadClass.values())
		{
			double drawn = rated(behaviour).drawAt(MINIMUM).value();

			assertEquals(behaviour == LoadClass.CONSTANT_RESISTANCE
				? 2.0
				: behaviour == LoadClass.CONSTANT_CURRENT ? 8.0 : 32.0, drawn, EXACT,
				behaviour + " does not run at its minimum");
		}
	}

	/** A device with no floor at all runs on whatever it is given, however little that is. */
	@Test
	void aDeviceWithNoFloorRunsOnWhateverItIsGiven()
	{
		Load unfloored = new Load(ID, LIVE, RETURN, LoadClass.CONSTANT_RESISTANCE, RATED_POWER, RATED_VOLTAGE,
			Volts.ZERO);

		assertEquals(0.25, unfloored.drawAt(new Volts(4.0)).value(), EXACT);
	}

	/** A nameplate naming no power, or no potential to take it at, names no device. */
	@Test
	void aNameplateNamingNoDeviceIsRefused()
	{
		assertThrows(IllegalArgumentException.class, () -> load(RATED_POWER, Volts.ZERO, MINIMUM));
		assertThrows(IllegalArgumentException.class, () -> load(Watts.ZERO, RATED_VOLTAGE, MINIMUM));
		assertThrows(IllegalArgumentException.class, () -> load(new Watts(-1024.0), RATED_VOLTAGE, MINIMUM));
		assertThrows(IllegalArgumentException.class, () -> load(RATED_POWER, new Volts(-128.0), MINIMUM));
	}

	/**
	 * A device that gives up above the potential it is measured at is not a device: it would never
	 * be running at the one figure its nameplate describes.
	 */
	@Test
	void aFloorAboveTheNameplateIsRefused()
	{
		assertThrows(IllegalArgumentException.class,
			() -> load(RATED_POWER, RATED_VOLTAGE, new Volts(RATED_VOLTAGE.value() + 1.0)));
		assertThrows(IllegalArgumentException.class, () -> load(RATED_POWER, RATED_VOLTAGE, new Volts(-1.0)));
	}

	/** A device wired to one node twice is across nothing, and cannot be asked what it draws. */
	@Test
	void aDeviceWiredToItselfIsRefused()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new Load(ID, LIVE, LIVE, LoadClass.CONSTANT_POWER, RATED_POWER, RATED_VOLTAGE, MINIMUM));
	}

	private static Load rated(LoadClass behaviour)
	{
		return new Load(ID, LIVE, RETURN, behaviour, RATED_POWER, RATED_VOLTAGE, MINIMUM);
	}

	/**
	 * Puts a device together from the three figures a nameplate carries, for the tests that expect
	 * the attempt to be refused. Nothing is handed back: what is being asked is whether such a
	 * device can be built at all.
	 */
	private static void load(Watts ratedPower, Volts ratedVoltage, Volts minimumVoltage)
	{
		new Load(ID, LIVE, RETURN, LoadClass.CONSTANT_POWER, ratedPower, ratedVoltage, minimumVoltage);
	}
}
