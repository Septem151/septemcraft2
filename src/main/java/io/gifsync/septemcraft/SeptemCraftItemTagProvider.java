package io.gifsync.septemcraft;

import io.gifsync.septemcraft.electrification.Electrification;
import io.gifsync.septemcraft.namespace.Namespace;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Every item tag the mod ships. The mod adds no blocks, so nothing here is copied from one. */
final class SeptemCraftItemTagProvider extends ItemTagsProvider
{
	private final Electrification electrification;

	SeptemCraftItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
		ExistingFileHelper existingFileHelper, Electrification electrification)
	{
		super(output, lookup, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), Namespace.ID,
			existingFileHelper);
		this.electrification = electrification;
	}

	@Override
	protected void addTags(HolderLookup.Provider provider)
	{
		electrification.tags()
			.forEach((tag, items) -> items.forEach(item -> tag(tag).add(item.get())));
	}
}
