package io.gifsync.septemcraft.processing;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/** A recipe the mod adds: where a data pack files it, and what it says. */
public sealed interface CreateRecipe permits ProcessingRecipe, SequencedAssembly
{
	/** The namespace Create registers under. */
	String NAMESPACE = "create";

	/** The recipe type Create registers under a folder of its own. */
	static ResourceLocation typeOf(String folder)
	{
		return ResourceLocation.fromNamespaceAndPath(NAMESPACE, folder);
	}

	/** The name this recipe is filed under, which is the item it is named for. */
	ResourceLocation name();

	/** The path Create's recipe type takes, and so the folder a data pack files this recipe in. */
	String folder();

	/** The id a data pack files this recipe under, and so the id the game knows it by. */
	default ResourceLocation id()
	{
		return ResourceLocation.fromNamespaceAndPath(name().getNamespace(), folder() + "/" + name().getPath());
	}

	/** The recipe type Create reads this recipe as. */
	default ResourceLocation type()
	{
		return typeOf(folder());
	}

	/** This recipe as the JSON a data pack holds. */
	JsonObject toJson();
}
