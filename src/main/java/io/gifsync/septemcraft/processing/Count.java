package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;

/** How many of an item a recipe produces. */
public record Count(int items)
{
	/** One item, which is what a recipe producing a single thing leaves unwritten. */
	public static final Count ONE = new Count(1);

	/** Checks that a recipe produces at least one item. */
	public Count
	{
		if (items < 1)
		{
			throw new IllegalArgumentException("A recipe produces at least one item, not " + items);
		}
	}

	/** Writes this count onto an output, which a single item leaves unwritten. */
	void writeTo(JsonObject output)
	{
		if (items != ONE.items)
		{
			output.addProperty("count", items);
		}
	}
}
