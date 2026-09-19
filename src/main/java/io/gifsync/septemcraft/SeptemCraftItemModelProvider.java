package io.gifsync.septemcraft;

import io.gifsync.septemcraft.electrification.Electrification;
import io.gifsync.septemcraft.namespace.Namespace;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Every item model the mod ships. */
final class SeptemCraftItemModelProvider extends ItemModelProvider
{
	private final Electrification electrification;

	SeptemCraftItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper,
		Electrification electrification)
	{
		super(output, Namespace.ID, existingFileHelper);
		this.electrification = electrification;
	}

	@Override
	protected void registerModels()
	{
		electrification.models(this);
	}
}
