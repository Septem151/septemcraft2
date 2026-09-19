package io.gifsync.septemcraft.electrification.material;

import io.gifsync.septemcraft.namespace.Namespace;
import io.gifsync.septemcraft.processing.ProcessingRecipe;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Checks that what the feature declares is what a loaded world actually holds. The recipes are
 * written as JSON by data generation and read back by Create's own serializers, so a key spelled
 * wrong costs nothing at build time and silently drops the recipe here.
 */
@GameTestHolder(Namespace.ID)
@PrefixGameTestTemplate(false)
public class MaterialRecipesTest
{
	/** An empty structure, because nothing under test needs anything placed in the world. */
	private static final String TEMPLATE = "empty";

	/** Every recipe the feature declares is loaded, under the Create recipe type it was written for. */
	@GameTest(template = TEMPLATE)
	public static void every_declared_recipe_loads_as_its_create_type(GameTestHelper helper)
	{
		RecipeManager recipes = helper.getLevel().getRecipeManager();

		for (ProcessingRecipe declared : new MaterialRecipes().all())
		{
			Recipe<?> loaded = recipes.byKey(declared.id()).orElse(null);
			helper.assertTrue(loaded != null, "No recipe loaded for " + declared.id());

			ResourceLocation type = loaded == null ? null : ForgeRegistries.RECIPE_TYPES.getKey(loaded.getType());
			helper.assertTrue(declared.process().type().equals(type),
				declared.id() + " loaded as " + type + " rather than " + declared.process().type());
		}

		helper.succeed();
	}

	/** Both materials reached the item registry under the names the recipes name them by. */
	@GameTest(template = TEMPLATE)
	public static void both_materials_are_registered(GameTestHelper helper)
	{
		for (String material : new String[]{Materials.MAGNETIC_ALLOY_INGOT, Materials.MAGNET})
		{
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Namespace.ID, material);
			helper.assertTrue(ForgeRegistries.ITEMS.containsKey(id), "No item registered as " + id);
		}

		helper.succeed();
	}
}
