package io.gifsync.septemcraft.electrification.material;

import io.gifsync.septemcraft.namespace.Namespace;
import io.gifsync.septemcraft.processing.ProcessingRecipe;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The materials an electrical machine is built from. */
public final class Materials
{
	/** Every ingot of magnetic alloy, whoever made it. */
	static final TagKey<Item> MAGNETIC_ALLOY_INGOTS = ItemTags
		.create(ResourceLocation.fromNamespaceAndPath("forge", "ingots/magnetic_alloy"));

	/** The alloy of sky stone and iron a magnet is pressed from. */
	static final String MAGNETIC_ALLOY_INGOT = "magnetic_alloy_ingot";

	/** The magnet an alternator segment carries. */
	static final String MAGNET = "magnet";

	private final RegistryObject<Item> magneticAlloyIngot;
	private final RegistryObject<Item> magnet;

	/** Registers the feature's items against the bus the loader is building the mod on. */
	public Materials(IEventBus modEventBus)
	{
		DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, Namespace.ID);
		magneticAlloyIngot = items.register(MAGNETIC_ALLOY_INGOT, () -> new Item(new Item.Properties()));
		magnet = items.register(MAGNET, () -> new Item(new Item.Properties()));
		items.register(modEventBus);
	}

	/** Every item the feature registers, in the order the chain makes them. */
	public List<RegistryObject<Item>> items()
	{
		return List.of(magneticAlloyIngot, magnet);
	}

	/** The magnet, which is what the chain exists to produce. */
	public RegistryObject<Item> magnet()
	{
		return magnet;
	}

	/** Names the feature's items in English. */
	public void translations(LanguageProvider provider)
	{
		provider.add(magneticAlloyIngot.get(), "Magnetic Alloy Ingot");
		provider.add(magnet.get(), "Magnet");
	}

	/** Models the feature's items as the flat sprites an ingredient wants. */
	public void models(ItemModelProvider provider)
	{
		items().forEach(item -> provider.basicItem(item.get()));
	}

	/** The Create processing that makes the feature's materials. */
	public List<ProcessingRecipe> recipes()
	{
		return new MaterialRecipes().all();
	}

	/** The tags the feature's items carry. */
	public Map<TagKey<Item>, List<RegistryObject<Item>>> tags()
	{
		return Map.of(MAGNETIC_ALLOY_INGOTS, List.of(magneticAlloyIngot));
	}
}
