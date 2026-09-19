package io.gifsync.septemcraft;

import io.gifsync.septemcraft.electrification.Electrification;
import io.gifsync.septemcraft.processing.ProcessingRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/**
 * Every recipe the mod adds. All of them are Create processing recipes, which are machine recipes
 * rather than anything a player crafts, so none of them carries an advancement.
 */
final class SeptemCraftRecipeProvider implements DataProvider
{
	private final PackOutput.PathProvider path;
	private final Electrification electrification;

	SeptemCraftRecipeProvider(PackOutput output, Electrification electrification)
	{
		path = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
		this.electrification = electrification;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output)
	{
		List<CompletableFuture<?>> written = new ArrayList<>();
		for (ProcessingRecipe recipe : electrification.recipes())
		{
			written.add(DataProvider.saveStable(output, recipe.toJson(), path.json(recipe.id())));
		}

		return CompletableFuture.allOf(written.toArray(new CompletableFuture<?>[0]));
	}

	@Override
	public String getName()
	{
		return "Processing recipes";
	}
}
