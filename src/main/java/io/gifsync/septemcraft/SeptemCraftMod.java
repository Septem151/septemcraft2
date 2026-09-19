package io.gifsync.septemcraft;

import io.gifsync.septemcraft.electrification.Electrification;
import io.gifsync.septemcraft.namespace.Namespace;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** The mod. Builds its modules, the menu they appear in, and the data they generate. */
@Mod(Namespace.ID)
public class SeptemCraftMod
{
	/** Assembles the mod against the bus the loader is building it on. */
	public SeptemCraftMod(FMLJavaModLoadingContext context)
	{
		IEventBus modEventBus = context.getModEventBus();
		Electrification electrification = new Electrification(modEventBus);

		new SeptemCraftCreativeTab(electrification).register(modEventBus);
		new SeptemCraftData(electrification).listenOn(modEventBus);
	}
}
