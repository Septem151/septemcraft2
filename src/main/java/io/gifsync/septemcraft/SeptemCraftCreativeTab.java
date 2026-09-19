package io.gifsync.septemcraft;

import io.gifsync.septemcraft.electrification.Electrification;
import io.gifsync.septemcraft.namespace.Namespace;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

/** The one menu tab the mod adds, holding everything every module registers. */
final class SeptemCraftCreativeTab
{
	/** The translation key the tab is titled by. */
	static final String TITLE = "itemGroup." + Namespace.ID;

	private final DeferredRegister<CreativeModeTab> tabs;

	/** Builds the tab around the modules whose contents fill it. */
	SeptemCraftCreativeTab(Electrification electrification)
	{
		tabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Namespace.ID);
		tabs.register(Namespace.ID, () -> CreativeModeTab.builder()
			.title(Component.translatable(TITLE))
			.icon(() -> new ItemStack(electrification.icon().get()))
			.displayItems((parameters, output) -> electrification.items()
				.forEach(item -> output.accept(item.get())))
			.build());
	}

	/** Registers the tab against the bus the loader is building the mod on. */
	void register(IEventBus modEventBus)
	{
		tabs.register(modEventBus);
	}
}
