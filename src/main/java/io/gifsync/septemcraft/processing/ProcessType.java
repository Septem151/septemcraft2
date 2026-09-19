package io.gifsync.septemcraft.processing;

import net.minecraft.resources.ResourceLocation;

/** A Create machine, and so the recipe type a recipe that runs on it carries. */
public enum ProcessType
{
	/** Crushing wheels. */
	CRUSHING("crushing"),

	/** A deployer, holding whatever it applies. */
	DEPLOYING("deploying"),

	/** A basin under a mechanical mixer. */
	MIXING("mixing"),

	/** A mechanical press. */
	PRESSING("pressing");

	private final String path;

	ProcessType(String path)
	{
		this.path = path;
	}

	/** The recipe type Create registers this machine's recipes under. */
	public ResourceLocation type()
	{
		return CreateRecipe.typeOf(path);
	}

	/** The folder a data pack files this machine's recipes in. */
	public String folder()
	{
		return path;
	}
}
