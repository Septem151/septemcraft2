package io.gifsync.septemcraft.electrification.circuit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Checks that a nameplate rating becomes the three behaviours a load class asks of it. */
class LoadTest
{
	private static final double EXACT = 1.0e-12;

	private static final NodeId LIVE = new NodeId(0);

	private static final NodeId RETURN = new NodeId(1);

	@Test
	void aNameplateGivesAResistanceAndARatedCurrent()
	{
		Load load = new Load(LIVE, RETURN, LoadClass.CONSTANT_RESISTANCE, new Watts(1024.0), new Volts(128.0));

		assertEquals(16.0, load.resistance().value(), EXACT);
		assertEquals(8.0, load.ratedCurrent().value(), EXACT);
	}

	@Test
	void aConstantResistanceLoadDrawsWhatItsResistanceAllows()
	{
		Load load = new Load(LIVE, RETURN, LoadClass.CONSTANT_RESISTANCE, new Watts(1024.0), new Volts(128.0));

		assertEquals(8.0, load.drawAt(new Volts(128.0)).value(), EXACT);
		assertEquals(4.0, load.drawAt(new Volts(64.0)).value(), EXACT);
	}

	@Test
	void aConstantCurrentLoadDrawsTheSameWhateverItIsGiven()
	{
		Load load = new Load(LIVE, RETURN, LoadClass.CONSTANT_CURRENT, new Watts(1024.0), new Volts(128.0));

		assertEquals(8.0, load.drawAt(new Volts(128.0)).value(), EXACT);
		assertEquals(8.0, load.drawAt(new Volts(64.0)).value(), EXACT);
	}

	@Test
	void aConstantPowerLoadDrawsMoreAsItsVoltageSags()
	{
		Load load = new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, new Watts(1024.0), new Volts(128.0));

		assertEquals(8.0, load.drawAt(new Volts(128.0)).value(), EXACT);
		assertEquals(16.0, load.drawAt(new Volts(64.0)).value(), EXACT);
	}

	@Test
	void aConstantPowerLoadOnADeadCircuitDrawsNothing()
	{
		Load load = new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, new Watts(1024.0), new Volts(128.0));

		assertEquals(0.0, load.drawAt(Volts.ZERO).value(), EXACT);
	}

	@Test
	void aNameplateNamingNoDeviceIsRefused()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, new Watts(1024.0), Volts.ZERO));
		assertThrows(IllegalArgumentException.class,
			() -> new Load(LIVE, RETURN, LoadClass.CONSTANT_POWER, Watts.ZERO, new Volts(128.0)));
		assertThrows(IllegalArgumentException.class,
			() -> new Load(LIVE, LIVE, LoadClass.CONSTANT_POWER, new Watts(1024.0), new Volts(128.0)));
	}
}
