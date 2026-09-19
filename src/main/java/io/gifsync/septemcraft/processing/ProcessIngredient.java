package io.gifsync.septemcraft.processing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
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

	/** Writes ingredients onto a recipe, under the key Create reads them from. */
	static void writeTo(JsonObject recipe, List<ProcessIngredient> ingredients)
	{
		JsonArray consumed = new JsonArray();
		ingredients.forEach(ingredient -> consumed.add(ingredient.toJson()));
		recipe.add("ingredients", consumed);
	}

	/** This ingredient as the JSON a data pack holds. */
	JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty(source.key(), name.toString());
		return json;
	}
}
