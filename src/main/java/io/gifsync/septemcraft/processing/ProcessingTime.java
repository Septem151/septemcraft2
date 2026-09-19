package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;

/** How long a machine takes over a recipe. */
public record ProcessingTime(int ticks)
{
	/** Checks that a recipe takes time to run. */
	public ProcessingTime
	{
		if (ticks < 1)
		{
			throw new IllegalArgumentException("A recipe takes at least one tick, not " + ticks);
		}
	}

	/** Writes this duration onto a recipe. */
	void writeTo(JsonObject recipe)
	{
		recipe.addProperty("processingTime", ticks);
	}
}
