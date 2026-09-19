package io.gifsync.septemcraft.electrification.circuit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Checks that the electrical quantities carry either sign and that crossing them lands on the right one. */
class QuantitiesTest
{
	/** How close two figures count as equal, which for arithmetic this direct is very close indeed. */
	private static final double EXACT = 1.0e-12;

	@Test
	void aQuantityOfEitherSignIsAQuantity()
	{
		assertEquals(-128.0, new Volts(-128.0).value(), EXACT);
		assertEquals(-3.8, new Amperes(-3.8).value(), EXACT);
		assertEquals(-463.0, new Watts(-463.0).value(), EXACT);
		assertEquals(-0.5, new Ohms(-0.5).value(), EXACT);
	}

	@Test
	void aQuantityThatIsNotANumberIsRefused()
	{
		assertThrows(IllegalArgumentException.class, () -> new Volts(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> new Amperes(Double.POSITIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> new Watts(Double.NEGATIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> new Ohms(Double.NaN));
	}

	@Test
	void voltsOverOhmsAreAmperes()
	{
		assertEquals(4.0, new Volts(12.0).over(new Ohms(3.0)).value(), EXACT);
	}

	@Test
	void voltsOverAmperesAreOhms()
	{
		assertEquals(3.0, new Volts(12.0).over(new Amperes(4.0)).value(), EXACT);
	}

	@Test
	void voltsTimesAmperesAreWatts()
	{
		assertEquals(1365.0, new Volts(128.0).times(new Amperes(10.6640625)).value(), EXACT);
	}

	@Test
	void amperesTimesOhmsAreVolts()
	{
		assertEquals(12.0, new Amperes(4.0).times(new Ohms(3.0)).value(), EXACT);
	}

	@Test
	void wattsOverVoltsAreAmperes()
	{
		assertEquals(8.0, new Watts(1024.0).over(new Volts(128.0)).value(), EXACT);
	}

	@Test
	void wattsOverAmperesAreVolts()
	{
		assertEquals(128.0, new Watts(1024.0).over(new Amperes(8.0)).value(), EXACT);
	}

	@Test
	void aCurrentDissipatesTheSquareOfItselfInAResistance()
	{
		assertEquals(341.3333333333, new Amperes(32.0 / 3.0).dissipatedIn(new Ohms(3.0)).value(), 1.0e-9);
	}

	@Test
	void aRunOfConductorResistsItsFormTimesItsLength()
	{
		assertEquals(1.5, ConductorForm.CATENARY_WIRE.resistanceOver(new Blocks(150)).value(), EXACT);
		assertEquals(0.036, ConductorForm.BUSBAR.resistanceOver(new Blocks(18)).value(), EXACT);
	}

	@Test
	void aTransformerKeepsWhatItDoesNotLose()
	{
		assertEquals(1024.0, new Watts(1024.0).lessLoss(LossFraction.NONE).value(), EXACT);
		assertEquals(972.8, new Watts(1024.0).lessLoss(new LossFraction(0.05)).value(), 1.0e-9);
	}

	@Test
	void aLossIsAShareOfWhatCrossesAndNotAllOfIt()
	{
		assertThrows(IllegalArgumentException.class, () -> new LossFraction(-0.1));
		assertThrows(IllegalArgumentException.class, () -> new LossFraction(1.0));
	}

	@Test
	void aTurnsRatioCanBeWoundTheOtherWay()
	{
		assertEquals(0.25, new TurnsRatio(4.0).inverted().ratio(), EXACT);
		assertEquals(512.0, new TurnsRatio(4.0).applyTo(new Volts(128.0)).value(), EXACT);
		assertThrows(IllegalArgumentException.class, () -> new TurnsRatio(0.0));
	}
}
