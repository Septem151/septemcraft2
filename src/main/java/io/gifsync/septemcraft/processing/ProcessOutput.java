package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;
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
