package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;
import java.util.Locale;

/** What a basin needs under it for a recipe to run. */
public enum HeatRequirement
{
	/** Nothing. The recipe runs on a bare basin. */
	NONE,

	/** A lit Blaze Burner. */
	HEATED,

	/** A Blaze Burner fed a Blaze Cake. */
	SUPERHEATED;

	/** Writes this requirement onto a recipe, which a recipe needing no heat leaves unwritten. */
	void writeTo(JsonObject recipe)
	{
		if (this != NONE)
		{
			recipe.addProperty("heatRequirement", name().toLowerCase(Locale.ROOT));
		}
	}
}
