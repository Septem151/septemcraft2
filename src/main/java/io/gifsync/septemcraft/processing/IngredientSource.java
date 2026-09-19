package io.gifsync.septemcraft.processing;

import java.util.Locale;

/** How an ingredient names what satisfies it. */
public enum IngredientSource
{
	/** One item, by its registry name. */
	ITEM,

	/** Every item carrying a tag. */
	TAG;

	/** The key this source takes in recipe JSON. */
	String key()
	{
		return name().toLowerCase(Locale.ROOT);
	}
}
