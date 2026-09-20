package io.gifsync.septemcraft.electrification.material;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import io.gifsync.septemcraft.namespace.Namespace;
import io.gifsync.septemcraft.processing.AssemblyStep;
import io.gifsync.septemcraft.processing.CreateRecipe;
import io.gifsync.septemcraft.processing.IngredientSource;
import io.gifsync.septemcraft.processing.ProcessIngredient;
import io.gifsync.septemcraft.processing.SequencedAssembly;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

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

	/** How many ingredients a step takes: the item an assembly carries, and the one applied to it. */
	private static final int INGREDIENTS_PER_STEP = 2;

	/** Every recipe the feature declares is loaded, under the Create recipe type it was written for. */
	@GameTest(template = TEMPLATE)
	public static void every_declared_recipe_loads_as_its_create_type(GameTestHelper helper)
	{
		RecipeManager recipes = helper.getLevel().getRecipeManager();

		for (CreateRecipe declared : new MaterialRecipes().all())
		{
			Recipe<?> loaded = recipes.byKey(declared.id()).orElse(null);
			helper.assertTrue(loaded != null, "No recipe loaded for " + declared.id());

			ResourceLocation type = loaded == null ? null : ForgeRegistries.RECIPE_TYPES.getKey(loaded.getType());
			helper.assertTrue(declared.type().equals(type),
				declared.id() + " loaded as " + type + " rather than " + declared.type());
		}

		helper.succeed();
	}

	/**
	 * Every assembly is loaded as the line it was written as. The recipe type says nothing about what
	 * an assembly starts from, how many passes it takes, what it carries between them, which machines
	 * it runs through, what those machines apply or what it ends as, and Create reads an assembly that
	 * names no loop count as running five.
	 */
	@GameTest(template = TEMPLATE)
	public static void every_declared_assembly_loads_as_the_line_it_declares(GameTestHelper helper)
	{
		RecipeManager recipes = helper.getLevel().getRecipeManager();
		int checked = 0;

		for (CreateRecipe declared : new MaterialRecipes().all())
		{
			if (declared instanceof SequencedAssembly assembly)
			{
				assertLine(helper, recipes, assembly);
				checked++;
			}
		}

		helper.assertTrue(checked > 0, "The feature declares no assembly, so this test checked nothing");
		helper.succeed();
	}

	/** Checks what a world loaded against the assembly it was written from. */
	private static void assertLine(GameTestHelper helper, RecipeManager recipes, SequencedAssembly declared)
	{
		if (!(recipes.byKey(declared.id()).orElse(null) instanceof SequencedAssemblyRecipe loaded))
		{
			helper.fail(declared.id() + " did not load as a sequenced assembly");
			return;
		}

		assertAccepts(helper, loaded.getIngredient(), declared.ingredient(), declared.id() + " starts from");

		String carried = nameOf(loaded.getTransitionalItem());
		helper.assertTrue(declared.carried().toString().equals(carried),
			declared.id() + " carries " + carried + " rather than " + declared.carried());

		helper.assertTrue(loaded.getLoops() == declared.loops().passes(),
			declared.id() + " runs " + loaded.getLoops() + " passes rather than " + declared.loops().passes());

		helper.assertTrue(loaded.resultPool.size() == 1,
			declared.id() + " produces " + loaded.resultPool.size() + " outcomes rather than the one declared");

		String result = nameOf(loaded.resultPool.get(0).getStack());
		helper.assertTrue(declared.result().toString().equals(result),
			declared.id() + " ends as " + result + " rather than " + declared.result());

		assertSequence(helper, declared, loaded.getSequence());
	}

	/** Checks the machines an assembly's passes run through, and what each applies, against its steps. */
	private static void assertSequence(GameTestHelper helper, SequencedAssembly declared,
		List<SequencedRecipe<?>> loaded)
	{
		helper.assertTrue(loaded.size() == declared.sequence().size(),
			declared.id() + " runs " + loaded.size() + " steps rather than " + declared.sequence().size());

		for (int step = 0; step < declared.sequence().size(); step++)
		{
			AssemblyStep expected = declared.sequence().get(step);
			Recipe<?> pass = loaded.get(step).getRecipe();

			ResourceLocation machine = ForgeRegistries.RECIPE_TYPES.getKey(pass.getType());
			helper.assertTrue(expected.process().type().equals(machine), declared.id() + " step " + step
				+ " runs on " + machine + " rather than " + expected.process().type());

			List<Ingredient> taken = pass.getIngredients();
			helper.assertTrue(taken.size() == INGREDIENTS_PER_STEP, declared.id() + " step " + step + " takes "
				+ taken.size() + " ingredients rather than the carried item and the one applied to it");

			if (taken.size() == INGREDIENTS_PER_STEP)
			{
				assertAccepts(helper, taken.get(1), expected.applied(), declared.id() + " step " + step + " applies");
			}
		}
	}

	/**
	 * Checks that an ingredient a world loaded accepts exactly the items the declared one names.
	 * Create rewrites the first ingredient of every step around what the assembly carries, so only
	 * the ingredients a step adds of its own are the feature's to answer for.
	 */
	private static void assertAccepts(GameTestHelper helper, Ingredient loaded, ProcessIngredient declared,
		String what)
	{
		List<String> named = namedBy(declared);
		helper.assertTrue(!named.isEmpty(), what + " nothing, because nothing is registered under "
			+ declared.name());

		List<String> accepted = Stream.of(loaded.getItems()).map(MaterialRecipesTest::nameOf).sorted().toList();
		helper.assertTrue(accepted.equals(named), what + " " + accepted + " rather than " + named);
	}

	/** Every item a declared ingredient names, by its own name or through the tag it names. */
	private static List<String> namedBy(ProcessIngredient declared)
	{
		if (declared.source() == IngredientSource.TAG)
		{
			ITagManager<Item> tags = Optional.ofNullable(ForgeRegistries.ITEMS.tags())
				.orElseThrow(() -> new IllegalStateException("The item registry holds no tags"));

			return tags.getTag(ItemTags.create(declared.name())).stream()
				.map(item -> String.valueOf(ForgeRegistries.ITEMS.getKey(item)))
				.sorted()
				.toList();
		}

		return List.of(declared.name().toString());
	}

	/** What a stack holds, by the name the item registry knows it under. */
	private static String nameOf(ItemStack stack)
	{
		return String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem()));
	}

	/** Every material reached the item registry under the name the recipes name it by. */
	@GameTest(template = TEMPLATE)
	public static void every_material_is_registered(GameTestHelper helper)
	{
		for (String material : new String[]{Materials.MAGNETIC_ALLOY_INGOT, Materials.MAGNET,
			Materials.INCOMPLETE_MAGNETIC_SHAFT, Materials.MAGNETIC_SHAFT})
		{
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Namespace.ID, material);
			helper.assertTrue(ForgeRegistries.ITEMS.containsKey(id), "No item registered as " + id);
		}

		helper.succeed();
	}
}
