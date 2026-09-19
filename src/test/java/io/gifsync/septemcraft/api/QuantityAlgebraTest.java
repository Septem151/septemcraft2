package io.gifsync.septemcraft.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks that the quantities agree with one another. Nothing here asserts a figure the mod is
 * balanced around; what it asserts is that crossing two quantities lands on the right one, and that
 * a reading which is not a number never gets as far as a circuit.
 */
class QuantityAlgebraTest
{
	/** How close two figures this directly related count as equal, which is very close indeed. */
	private static final double EXACT = 1.0e-12;

	/**
	 * A quantity that is not a number poisons every figure derived from it and says nothing about
	 * where it came from, so it is refused where it enters rather than found later.
	 */
	@Test
	void aReadingThatIsNotANumberIsRefused()
	{
		assertThrows(IllegalArgumentException.class, () -> new Volts(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> new Amperes(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> new Watts(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> new Ohms(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> new Rpm(Double.NaN));

		assertThrows(IllegalArgumentException.class, () -> new Volts(Double.POSITIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> new Amperes(Double.NEGATIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> new Watts(Double.POSITIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> new Ohms(Double.POSITIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> new Rpm(Double.NEGATIVE_INFINITY));
	}

	/**
	 * Every way of crossing three of these quantities to reach the fourth agrees with every other
	 * way, which is what catches a pair of operands written the wrong way round.
	 */
	@Test
	void theUnitsAgreeWhicheverWayTheyAreCrossed()
	{
		Volts potential = new Volts(12.0);
		Ohms resistance = new Ohms(3.0);
		Amperes current = potential.over(resistance);
		Watts power = potential.times(current);

		assertEquals(4.0, current.value(), EXACT);
		assertEquals(48.0, power.value(), EXACT);

		assertEquals(resistance.value(), potential.over(current).value(), EXACT);
		assertEquals(potential.value(), current.times(resistance).value(), EXACT);
		assertEquals(current.value(), power.over(potential).value(), EXACT);
		assertEquals(potential.value(), power.over(current).value(), EXACT);
	}

	/**
	 * A resistance burns the square of the current in it, so a current running the other way burns
	 * exactly as much. Losing that sign the wrong way round makes a conductor a source.
	 */
	@Test
	void aCurrentBurnsTheSameWhicheverWayItRuns()
	{
		Ohms resistance = new Ohms(3.0);

		assertEquals(192.0, new Amperes(8.0).dissipatedIn(resistance).value(), EXACT);
		assertEquals(192.0, new Amperes(-8.0).dissipatedIn(resistance).value(), EXACT);
	}

	/** A transformer wound the other way undoes the first, and one wound no turns at all is no transformer. */
	@Test
	void aTurnsRatioCanBeWoundTheOtherWayAndBackAgain()
	{
		TurnsRatio step = new TurnsRatio(4.0);

		assertEquals(0.25, step.inverted().ratio(), EXACT);
		assertEquals(step.ratio(), step.inverted().inverted().ratio(), EXACT);
		assertEquals(512.0, step.applyTo(new Volts(128.0)).value(), EXACT);
		assertEquals(128.0, step.inverted().applyTo(step.applyTo(new Volts(128.0))).value(), EXACT);

		assertThrows(IllegalArgumentException.class, () -> new TurnsRatio(0.0));
	}

	/**
	 * A loss is a share of what crosses. All of it leaves nothing to arrive and more than all of it
	 * would have a transformer deliver backwards, so neither is a loss.
	 */
	@Test
	void aLossIsAShareOfWhatCrossesAndNotAllOfIt()
	{
		assertEquals(0.0, LossFraction.NONE.fraction(), EXACT);

		assertThrows(IllegalArgumentException.class, () -> new LossFraction(-0.1));
		assertThrows(IllegalArgumentException.class, () -> new LossFraction(1.0));
		assertThrows(IllegalArgumentException.class, () -> new LossFraction(1.5));
	}

	/** What a crossing keeps is what it was given less the share it loses. */
	@Test
	void aCrossingKeepsWhatItDoesNotLose()
	{
		assertEquals(1024.0, new Watts(1024.0).lessLoss(LossFraction.NONE).value(), EXACT);
		assertEquals(972.8, new Watts(1024.0).lessLoss(new LossFraction(0.05)).value(), 1.0e-9);
	}
}
