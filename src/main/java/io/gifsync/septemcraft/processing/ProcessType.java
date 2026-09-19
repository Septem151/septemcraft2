package io.gifsync.septemcraft.processing;

import net.minecraft.resources.ResourceLocation;

/** A Create machine, and so the recipe type a processing recipe carries. */
public enum ProcessType
{
	/** Crushing wheels. */
	CRUSHING("crushing"),

	/** A basin under a mechanical mixer. */
	MIXING("mixing"),

	/** A mechanical press. */
	PRESSING("pressing");

	/** The namespace Create registers its recipe types under. */
	private static final String CREATE = "create";

	private final String path;

	ProcessType(String path)
	{
		this.path = path;
	}

	/** The recipe type Create registers this machine's recipes under. */
	public ResourceLocation type()
	{
		return ResourceLocation.fromNamespaceAndPath(CREATE, path);
	}

	/** The folder a data pack files this machine's recipes in. */
	public String folder()
	{
		return path;
	}
}
