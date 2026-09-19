package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/** One Create processing recipe: what it consumes, what it produces, and what it runs on. */
public record ProcessingRecipe(ResourceLocation name, ProcessType process, List<ProcessIngredient> ingredients,
	List<ProcessOutput> results, HeatRequirement heat, Optional<ProcessingTime> time) implements CreateRecipe
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

	@Override
	public String folder()
	{
		return process.folder();
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("type", type().toString());
		heat.writeTo(json);

		ProcessIngredient.writeTo(json, ingredients);
		ProcessOutput.writeTo(json, results);

		time.ifPresent(duration -> duration.writeTo(json));
		return json;
	}
}
