package io.gifsync.septemcraft.electrification;

import io.gifsync.septemcraft.electrification.material.Materials;
import io.gifsync.septemcraft.processing.ProcessingRecipe;
import java.util.List;
import java.util.Map;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

/** The electrification module: its features, and everything the mod root asks of them. */
public final class Electrification
{
	private final Materials materials;

	/** Builds the module's features, registering what each of them adds. */
	public Electrification(IEventBus modEventBus)
	{
		materials = new Materials(modEventBus);
	}

	/** Every item the module registers, in the order a menu should show them. */
	public List<RegistryObject<Item>> items()
	{
		return materials.items();
	}

	/** The item that stands for the module in a menu. */
	public RegistryObject<Item> icon()
	{
		return materials.magnet();
	}

	/** Names everything the module registers in English. */
	public void translations(LanguageProvider provider)
	{
		materials.translations(provider);
	}

	/** Models everything the module registers. */
	public void models(ItemModelProvider provider)
	{
		materials.models(provider);
	}

	/** Every processing recipe the module adds. */
	public List<ProcessingRecipe> recipes()
	{
		return materials.recipes();
	}

	/** The tags the module's items carry. */
	public Map<TagKey<Item>, List<RegistryObject<Item>>> tags()
	{
		return materials.tags();
	}
}
