package io.gifsync.septemcraft;

import io.gifsync.septemcraft.electrification.Electrification;
import io.gifsync.septemcraft.namespace.Namespace;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Every English name the mod uses. One locale is one file, so one provider writes all of it and the
 * modules fill it.
 */
final class SeptemCraftLanguageProvider extends LanguageProvider
{
	private final Electrification electrification;

	SeptemCraftLanguageProvider(PackOutput output, Electrification electrification)
	{
		super(output, Namespace.ID, "en_us");
		this.electrification = electrification;
	}

	@Override
	protected void addTranslations()
	{
		add(SeptemCraftCreativeTab.TITLE, "SeptemCraft");
		electrification.translations(this);
	}
}
