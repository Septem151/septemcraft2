package io.gifsync.septemcraft.electrification.material;

import io.gifsync.septemcraft.namespace.Namespace;
import io.gifsync.septemcraft.processing.Chance;
import io.gifsync.septemcraft.processing.Count;
import io.gifsync.septemcraft.processing.HeatRequirement;
import io.gifsync.septemcraft.processing.ProcessIngredient;
import io.gifsync.septemcraft.processing.ProcessOutput;
import io.gifsync.septemcraft.processing.ProcessType;
import io.gifsync.septemcraft.processing.ProcessingRecipe;
import io.gifsync.septemcraft.processing.ProcessingTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;

/** The Create processing that turns sky stone into magnets. */
final class MaterialRecipes
{
	/** The namespace Applied Energistics 2 registers under. */
	private static final String AE2 = "ae2";

	/** Sky stone as a meteorite leaves it, before anything has been done to it. */
	private static final ResourceLocation SKY_STONE = ResourceLocation.fromNamespaceAndPath(AE2, "sky_stone_block");

	/** The dust AE2 already has a name for, which the crushing wheels make rather than a grindstone. */
	private static final ResourceLocation SKY_DUST = ResourceLocation.fromNamespaceAndPath(AE2, "sky_dust");

	/** How much iron an ingot of alloy is stretched across. */
	private static final int IRON_PER_MIX = 3;

	/** Every recipe the feature adds, in the order the chain runs. */
	List<ProcessingRecipe> all()
	{
		return List.of(crushSkyStone(), mixMagneticAlloy(), pressMagnet());
	}

	/**
	 * Sky stone to dust. The wheels sometimes hand the block back whole, which is what automating the
	 * crushing buys over AE2's own grindstone.
	 */
	private ProcessingRecipe crushSkyStone()
	{
		return new ProcessingRecipe(
			ResourceLocation.fromNamespaceAndPath(Namespace.ID, "sky_stone_block"),
			ProcessType.CRUSHING,
			List.of(ProcessIngredient.item(SKY_STONE)),
			List.of(ProcessOutput.always(SKY_DUST), ProcessOutput.sometimes(SKY_STONE, new Chance(0.25F))),
			HeatRequirement.NONE,
			Optional.of(new ProcessingTime(250)));
	}

	/**
	 * Dust and iron to alloy, over a Blaze Burner. Iron is the bulk of it, so a meteorite's worth of
	 * sky stone stretches across an iron industry rather than being consumed ingot for ingot.
	 */
	private ProcessingRecipe mixMagneticAlloy()
	{
		List<ProcessIngredient> ingredients = new ArrayList<>();
		ingredients.add(ProcessIngredient.item(SKY_DUST));
		for (int ingot = 0; ingot < IRON_PER_MIX; ingot++)
		{
			ingredients.add(ProcessIngredient.tag(Tags.Items.INGOTS_IRON));
		}

		return new ProcessingRecipe(
			ResourceLocation.fromNamespaceAndPath(Namespace.ID, Materials.MAGNETIC_ALLOY_INGOT),
			ProcessType.MIXING,
			ingredients,
			List.of(ProcessOutput.always(
				ResourceLocation.fromNamespaceAndPath(Namespace.ID, Materials.MAGNETIC_ALLOY_INGOT),
				new Count(2))),
			HeatRequirement.HEATED,
			Optional.empty());
	}

	/** Alloy to magnet, under a press. */
	private ProcessingRecipe pressMagnet()
	{
		return new ProcessingRecipe(
			ResourceLocation.fromNamespaceAndPath(Namespace.ID, Materials.MAGNET),
			ProcessType.PRESSING,
			List.of(ProcessIngredient.tag(Materials.MAGNETIC_ALLOY_INGOTS)),
			List.of(ProcessOutput.always(ResourceLocation.fromNamespaceAndPath(Namespace.ID, Materials.MAGNET))),
			HeatRequirement.NONE,
			Optional.empty());
	}
}
