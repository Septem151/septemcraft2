package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;

/** How many times an assembly runs its sequence over one item. */
public record Loops(int passes)
{
	/** Checks that an assembly runs its sequence at least once. */
	public Loops
	{
		if (passes < 1)
		{
			throw new IllegalArgumentException("An assembly runs its sequence at least once, not " + passes);
		}
	}

	/** Writes this count onto an assembly. */
	void writeTo(JsonObject assembly)
	{
		assembly.addProperty("loops", passes);
	}
}
