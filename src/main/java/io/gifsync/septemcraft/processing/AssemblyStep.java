package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/** One machine an assembly sends its item through, and what that machine applies to it. */
public record AssemblyStep(ProcessType process, ProcessIngredient applied)
{
	/**
	 * The machines a step may name. A step is written as the carried item alongside the thing
	 * applied to it, so a machine that takes only one ingredient, or that Create never runs inside
	 * an assembly at all, would be written as a recipe Create drops on load.
	 */
	private static final Set<ProcessType> APPLIES_SOMETHING = Set.of(ProcessType.DEPLOYING);

	/** Checks that a step names a machine that applies something to what it is handed. */
	public AssemblyStep
	{
		if (!APPLIES_SOMETHING.contains(process))
		{
			throw new IllegalArgumentException("An assembly applies nothing on " + process);
		}
	}

	/** A pass under a deployer, which consumes what it holds. */
	public static AssemblyStep deploying(ProcessIngredient held)
	{
		return new AssemblyStep(ProcessType.DEPLOYING, held);
	}

	/** This step as the JSON a data pack holds, around the item an assembly carries between steps. */
	JsonObject toJson(ResourceLocation carried)
	{
		JsonObject json = new JsonObject();
		json.addProperty("type", process.type().toString());
		ProcessIngredient.writeTo(json, List.of(ProcessIngredient.item(carried), applied));
		ProcessOutput.writeTo(json, List.of(ProcessOutput.always(carried)));
		return json;
	}
}
