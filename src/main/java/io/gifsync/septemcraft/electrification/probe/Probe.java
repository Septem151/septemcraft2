package io.gifsync.septemcraft.electrification.probe;

import io.gifsync.septemcraft.namespace.Namespace;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The instrument that reads the electrical model against a running world. */
public final class Probe
{
	/** The probe held against a shaft to read what a circuit driven by that shaft would do. */
	static final String CIRCUIT_PROBE = "circuit_probe";

	private final RegistryObject<Item> circuitProbe;

	/** Registers the feature's items against the bus the loader is building the mod on. */
	public Probe(IEventBus modEventBus)
	{
		DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, Namespace.ID);
		circuitProbe = items.register(CIRCUIT_PROBE,
			() -> new CircuitProbeItem(new Item.Properties().stacksTo(1)));
		items.register(modEventBus);
	}

	/** Every item the feature registers. */
	public List<RegistryObject<Item>> items()
	{
		return List.of(circuitProbe);
	}

	/** Names the feature's items in English. */
	public void translations(LanguageProvider provider)
	{
		provider.add(circuitProbe.get(), "Circuit Probe");
	}

	/** Models the feature's items as the flat sprites an item in the hand wants. */
	public void models(ItemModelProvider provider)
	{
		items().forEach(item -> provider.basicItem(item.get()));
	}
}
