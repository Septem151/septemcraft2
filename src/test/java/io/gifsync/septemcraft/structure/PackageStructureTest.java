package io.gifsync.septemcraft.structure;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Checks that every package declares what kind it is, and that no class imports across a boundary
 * its package's kind forbids.
 */
@AnalyzeClasses(
		packages = "io.gifsync.septemcraft",
		importOptions = {ImportOption.DoNotIncludeTests.class, PackageStructureTest.DoNotIncludeGameTests.class})
class PackageStructureTest
{
	/** The package trees that exist only on a client, and so are absent from a dedicated server. */
	private static final List<String> CLIENT_ONLY = List.of("net.minecraft.client", "net.minecraftforge.client");

	/** Every package holding at least one class, so that a package is reported once however many it holds. */
	private static final ClassesTransformer<JavaPackage> PACKAGES =
			new AbstractClassesTransformer<>("packages")
			{
				@Override
				public Iterable<JavaPackage> doTransform(JavaClasses classes)
				{
					Set<JavaPackage> packages = new LinkedHashSet<>();
					for (JavaClass item : classes)
					{
						packages.add(item.getPackage());
					}

					return packages;
				}
			};

	@ArchTest
	static final ArchRule every_package_declares_its_kind = all(PACKAGES)
			.should(new ArchCondition<JavaPackage>("declare a kind")
			{
				@Override
				public void check(JavaPackage item, ConditionEvents events)
				{
					if (kindOf(item).isEmpty())
					{
						events.add(SimpleConditionEvent.violated(item,
								item.getName() + " has no package-info.java declaring a @PackageKind"));
					}
				}
			});

	@ArchTest
	static final ArchRule a_feature_depends_on_no_other_feature = classes()
			.should(new ArchCondition<JavaClass>("depend on no other feature")
			{
				@Override
				public void check(JavaClass item, ConditionEvents events)
				{
					Optional<String> feature = featureOf(item);
					if (feature.isEmpty())
					{
						return;
					}

					for (Dependency dependency : item.getDirectDependenciesFromSelf())
					{
						Optional<String> target = featureOf(dependency.getTargetClass());
						if (target.isPresent() && !target.get().equals(feature.get()))
						{
							events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
						}
					}
				}
			});

	@ArchTest
	static final ArchRule a_shared_package_depends_only_on_shared_packages = classes()
			.should(new ArchCondition<JavaClass>("depend on neither a feature nor a composition root")
			{
				@Override
				public void check(JavaClass item, ConditionEvents events)
				{
					if (kindOf(item.getPackage()).orElse(null) != Kind.SHARED)
					{
						return;
					}

					for (Dependency dependency : item.getDirectDependenciesFromSelf())
					{
						Kind target = kindOf(dependency.getTargetClass().getPackage()).orElse(null);
						if (target == Kind.FEATURE || target == Kind.COMPOSITION_ROOT)
						{
							events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
						}
					}
				}
			});

	@ArchTest
	static final ArchRule a_composition_root_is_imported_only_by_one_containing_it = classes()
			.should(new ArchCondition<JavaClass>("depend on no composition root but one enclosing it")
			{
				@Override
				public void check(JavaClass item, ConditionEvents events)
				{
					for (Dependency dependency : item.getDirectDependenciesFromSelf())
					{
						JavaClass target = dependency.getTargetClass();
						if (kindOf(target.getPackage()).orElse(null) != Kind.COMPOSITION_ROOT)
						{
							continue;
						}

						if (!encloses(item.getPackageName(), target.getPackageName()))
						{
							events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
						}
					}
				}
			});

	@ArchTest
	static final ArchRule a_package_in_a_feature_exposes_one_public_type = all(PACKAGES)
			.should(new ArchCondition<JavaPackage>("expose no more than one public type")
			{
				@Override
				public void check(JavaPackage item, ConditionEvents events)
				{
					if (kindOf(item).orElse(null) != Kind.FEATURE)
					{
						return;
					}

					List<String> exposed = item.getClasses().stream()
							.filter(PackageStructureTest::isExposed)
							.map(JavaClass::getSimpleName)
							.sorted()
							.toList();

					if (exposed.size() > 1)
					{
						events.add(SimpleConditionEvent.violated(item, item.getName()
								+ " exposes " + exposed.size() + " public types: " + String.join(", ", exposed)));
					}
				}
			});

	@ArchTest
	static final ArchRule a_package_inside_a_feature_is_part_of_it = all(PACKAGES)
			.should(new ArchCondition<JavaPackage>("declare itself a feature when a feature encloses it")
			{
				@Override
				public void check(JavaPackage item, ConditionEvents events)
				{
					if (kindOf(item).orElse(null) == Kind.FEATURE)
					{
						return;
					}

					enclosingFeature(item).ifPresent(feature -> events.add(SimpleConditionEvent.violated(item,
							item.getName() + " lies inside feature " + feature + " without declaring itself one")));
				}
			});

	@ArchTest
	static final ArchRule client_only_code_lives_in_a_client_package = classes()
			.should(new ArchCondition<JavaClass>("touch client-only types only inside a client package")
			{
				@Override
				public void check(JavaClass item, ConditionEvents events)
				{
					if (isClientPackage(item.getPackageName()))
					{
						return;
					}

					for (Dependency dependency : item.getDirectDependenciesFromSelf())
					{
						if (isClientOnly(dependency.getTargetClass()))
						{
							events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
						}
					}
				}
			});

	/** The kind a package declares, if it declares one. */
	private static Optional<Kind> kindOf(JavaPackage javaPackage)
	{
		return javaPackage.tryGetAnnotationOfType(PackageKind.class).map(PackageKind::value);
	}

	/**
	 * Names the feature a class belongs to, if it belongs to one. A feature may hold subpackages, so
	 * the feature is the outermost package declaring {@link Kind#FEATURE} at or above the class.
	 */
	private static Optional<String> featureOf(JavaClass item)
	{
		JavaPackage javaPackage = item.getPackage();
		if (kindOf(javaPackage).orElse(null) != Kind.FEATURE)
		{
			return Optional.empty();
		}

		return Optional.of(enclosingFeature(javaPackage).orElseGet(javaPackage::getName));
	}

	/** Names the outermost feature enclosing a package, if any feature encloses it. */
	private static Optional<String> enclosingFeature(JavaPackage javaPackage)
	{
		Optional<String> feature = Optional.empty();
		for (Optional<JavaPackage> ancestor = javaPackage.getParent();
			 ancestor.isPresent();
			 ancestor = ancestor.get().getParent())
		{
			if (kindOf(ancestor.get()).orElse(null) == Kind.FEATURE)
			{
				feature = Optional.of(ancestor.get().getName());
			}
		}

		return feature;
	}

	/** Whether one package is the other, or holds it at any depth. */
	private static boolean encloses(String outer, String inner)
	{
		return outer.equals(inner) || inner.startsWith(outer + ".");
	}

	/** Whether a class is a public type of its package, rather than a nested or package-private one. */
	private static boolean isExposed(JavaClass item)
	{
		return item.getModifiers().contains(JavaModifier.PUBLIC)
				&& !item.getName().contains("$")
				&& !item.getSimpleName().equals("package-info");
	}

	/** Whether a package is a {@code client} package, or lies inside one. */
	private static boolean isClientPackage(String name)
	{
		return name.endsWith(".client") || name.contains(".client.");
	}

	/** Whether a class is one that exists only on a client. */
	private static boolean isClientOnly(JavaClass item)
	{
		String name = item.getPackageName();
		return CLIENT_ONLY.stream().anyMatch(each -> encloses(each, name));
	}

	/** Keeps the gametest source set out of the analysis, the way ArchUnit keeps {@code src/test} out. */
	public static final class DoNotIncludeGameTests implements ImportOption
	{
		private static final Pattern GAMETEST_OUTPUT = Pattern.compile(".*/build/classes/([^/]+/)?gametest/.*");

		/** Whether a location holds classes the rules apply to. */
		@Override
		public boolean includes(Location location)
		{
			return !location.matches(GAMETEST_OUTPUT);
		}
	}
}
