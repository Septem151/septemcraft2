package io.gifsync.septemcraft.processing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/** One Create processing recipe: what it consumes, what it produces, and what it runs on. */
public record ProcessingRecipe(ResourceLocation name, ProcessType process, List<ProcessIngredient> ingredients,
	List<ProcessOutput> results, HeatRequirement heat, Optional<ProcessingTime> time)
{
	/** Checks that a recipe consumes something and produces something, and fixes what it holds. */
	public ProcessingRecipe
	{
		if (ingredients.isEmpty())
		{
			throw new IllegalArgumentException("A recipe consumes something: " + name);
		}

		if (results.isEmpty())
		{
			throw new IllegalArgumentException("A recipe produces something: " + name);
		}

		ingredients = List.copyOf(ingredients);
		results = List.copyOf(results);
	}

	/** The id a data pack files this recipe under, and so the id the game knows it by. */
	public ResourceLocation id()
	{
		return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), process.folder() + "/" + name.getPath());
	}

	/** This recipe as the JSON a data pack holds. */
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("type", process.type().toString());
		heat.writeTo(json);

		JsonArray consumed = new JsonArray();
		ingredients.forEach(ingredient -> consumed.add(ingredient.toJson()));
		json.add("ingredients", consumed);

		JsonArray produced = new JsonArray();
		results.forEach(result -> produced.add(result.toJson()));
		json.add("results", produced);

		time.ifPresent(duration -> duration.writeTo(json));
		return json;
	}
}
