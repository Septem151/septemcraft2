package io.gifsync.septemcraft.processing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/** One thing a processing recipe produces, how many of it, and how often. */
public record ProcessOutput(ResourceLocation item, Count count, Chance chance)
{
	/** An output produced once, every time. */
	public static ProcessOutput always(ResourceLocation item)
	{
		return new ProcessOutput(item, Count.ONE, Chance.CERTAIN);
	}

	/** An output produced a given number of times, every time. */
	public static ProcessOutput always(ResourceLocation item, Count count)
	{
		return new ProcessOutput(item, count, Chance.CERTAIN);
	}

	/** An output produced once, at the given odds. */
	public static ProcessOutput sometimes(ResourceLocation item, Chance chance)
	{
		return new ProcessOutput(item, Count.ONE, chance);
	}

	/** Writes outputs onto a recipe, under the key Create reads them from. */
	static void writeTo(JsonObject recipe, List<ProcessOutput> results)
	{
		JsonArray produced = new JsonArray();
		results.forEach(result -> produced.add(result.toJson()));
		recipe.add("results", produced);
	}

	/** This output as the JSON a data pack holds. */
	JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		count.writeTo(json);
		chance.writeTo(json);
		json.addProperty("item", item.toString());
		return json;
	}
}
