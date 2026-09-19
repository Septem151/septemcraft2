package io.gifsync.septemcraft.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks how a run of conductor resists, without saying how much. What each form resists per block
 * is a figure the mod is balanced on and may be retuned at any time; that a run twice as long
 * resists twice as much, and that a bar resists less than a wire, is not.
 */
class ConductorFormTest
{
	private static final double EXACT = 1.0e-12;

	/** Doubling a run doubles what it resists, whatever one block of it comes to. */
	@Test
	void aRunResistsInProportionToItsLength()
	{
		for (ConductorForm form : ConductorForm.values())
		{
			double single = form.resistanceOver(new Blocks(1)).value();

			assertEquals(single, form.perBlock().value(), EXACT, form + " disagrees with itself over one block");
			assertEquals(10.0 * single, form.resistanceOver(new Blocks(10)).value(), 1.0e-9);
			assertEquals(200.0 * single, form.resistanceOver(new Blocks(200)).value(), 1.0e-9);
		}
	}

	/** A run of no length is two terminals touching, and resists nothing. */
	@Test
	void aRunOfNoLengthResistsNothing()
	{
		for (ConductorForm form : ConductorForm.values())
		{
			assertEquals(0.0, form.resistanceOver(Blocks.NONE).value(), EXACT, form + " resists over no length");
		}
	}

	/** A solid bar is the better conductor of the two, which is what makes it worth what it costs. */
	@Test
	void aBusbarResistsLessPerBlockThanCatenaryWire()
	{
		assertTrue(ConductorForm.BUSBAR.perBlock().value() < ConductorForm.CATENARY_WIRE.perBlock().value(),
			"a busbar does not conduct better than a wire");
		assertTrue(ConductorForm.BUSBAR.perBlock().value() > 0.0, "a busbar resists nothing at all");
	}
}
