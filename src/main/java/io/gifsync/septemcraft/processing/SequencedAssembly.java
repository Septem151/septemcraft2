package io.gifsync.septemcraft.processing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * One Create sequenced assembly: what it starts from, the item it carries between passes, the
 * machines each pass sends that item through, how many times round, and what it finally becomes.
 */
public record SequencedAssembly(ProcessIngredient ingredient, ResourceLocation carried,
	List<AssemblyStep> sequence, Loops loops, ResourceLocation result) implements CreateRecipe
{
	/** The path Create's recipe type takes, and the folder a data pack files an assembly in. */
	private static final String SEQUENCED_ASSEMBLY = "sequenced_assembly";

	/** Checks that an assembly does something to what it is given, and fixes what it holds. */
	public SequencedAssembly
	{
		if (sequence.isEmpty())
		{
			throw new IllegalArgumentException("An assembly runs at least one step: " + result);
		}

		sequence = List.copyOf(sequence);
	}

	/** An assembly is filed under what it finally produces. */
	@Override
	public ResourceLocation name()
	{
		return result;
	}

	@Override
	public String folder()
	{
		return SEQUENCED_ASSEMBLY;
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("type", type().toString());
		json.add("ingredient", ingredient.toJson());
		json.add("transitionalItem", ProcessOutput.always(carried).toJson());

		JsonArray steps = new JsonArray();
		sequence.forEach(step -> steps.add(step.toJson(carried)));
		json.add("sequence", steps);

		ProcessOutput.writeTo(json, List.of(ProcessOutput.always(result)));

		loops.writeTo(json);
		return json;
	}
}
