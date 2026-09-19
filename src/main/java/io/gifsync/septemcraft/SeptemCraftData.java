package io.gifsync.septemcraft;

import io.gifsync.septemcraft.electrification.Electrification;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Data generation. Enumerates the modules and hands each provider the one thing it writes, so that
 * a module's own lang, models, recipes and tags reach the pack without the mod root knowing what
 * any of them say.
 */
final class SeptemCraftData
{
	private final Electrification electrification;

	/** Builds data generation around the modules that fill it. */
	SeptemCraftData(Electrification electrification)
	{
		this.electrification = electrification;
	}

	/** Listens for the loader asking for generated data. */
	void listenOn(IEventBus modEventBus)
	{
		modEventBus.addListener(this::gather);
	}

	private void gather(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();

		generator.addProvider(event.includeClient(),
			new SeptemCraftLanguageProvider(output, electrification));
		generator.addProvider(event.includeClient(),
			new SeptemCraftItemModelProvider(output, event.getExistingFileHelper(), electrification));
		generator.addProvider(event.includeServer(),
			new SeptemCraftRecipeProvider(output, electrification));
		generator.addProvider(event.includeServer(),
			new SeptemCraftItemTagProvider(output, event.getLookupProvider(),
				event.getExistingFileHelper(), electrification));
	}
}
