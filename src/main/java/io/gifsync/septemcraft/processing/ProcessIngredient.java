package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** One thing a processing recipe consumes: an item, or anything carrying a tag. */
public record ProcessIngredient(IngredientSource source, ResourceLocation name)
{
	/** An ingredient satisfied by one item. */
	public static ProcessIngredient item(ResourceLocation item)
	{
		return new ProcessIngredient(IngredientSource.ITEM, item);
	}

	/** An ingredient satisfied by anything carrying a tag. */
	public static ProcessIngredient tag(TagKey<Item> tag)
	{
		return new ProcessIngredient(IngredientSource.TAG, tag.location());
	}

	/** This ingredient as the JSON a data pack holds. */
	JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty(source.key(), name.toString());
		return json;
	}
}
