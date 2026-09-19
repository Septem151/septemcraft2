package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;

/** How often an output actually appears. */
public record Chance(float odds)
{
	/** Every time, which is what a guaranteed output leaves unwritten. */
	public static final Chance CERTAIN = new Chance(1.0F);

	/** Checks that an output appears sometimes and no more than always. */
	public Chance
	{
		if (!(odds > 0.0F) || odds > 1.0F)
		{
			throw new IllegalArgumentException("Odds fall between 0 and 1, not " + odds);
		}
	}

	/** Writes these odds onto an output, which a certain output leaves unwritten. */
	void writeTo(JsonObject output)
	{
		if (odds < CERTAIN.odds)
		{
			output.addProperty("chance", odds);
		}
	}
}
