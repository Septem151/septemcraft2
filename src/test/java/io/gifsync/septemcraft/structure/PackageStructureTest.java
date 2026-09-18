package io.gifsync.septemcraft.structure;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Checks that every package declares what kind it is, and that no class imports across a boundary
 * its package's kind forbids.
 */
@AnalyzeClasses(packages = PackageStructureTest.ROOT, importOptions = {ImportOption.DoNotIncludeTests.class,
	DoNotIncludeGameTests.class})
class PackageStructureTest
{
	/** The package tree these rules apply to. */
	static final String ROOT = "io.gifsync.septemcraft";

	/**
	 * Every package that holds a class, and every package of the mod above one, so that a package is
	 * reported once however many classes it holds and one holding only subpackages is reported too.
	 */
	private static final ClassesTransformer<JavaPackage> PACKAGES = new AbstractClassesTransformer<>("packages")
	{
		@Override
		public Iterable<JavaPackage> doTransform(JavaClasses classes)
		{
			Set<JavaPackage> packages = new LinkedHashSet<>();
			for (JavaClass item : classes)
			{
				packages.add(item.getPackage());
				for (JavaPackage ancestor : ancestorsOf(item.getPackage()))
				{
					if (isWithinRoot(ancestor))
					{
						packages.add(ancestor);
					}
				}
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
					events.add(SimpleConditionEvent.violated(
						item, item.getName() + " has no package-info.java declaring a @PackageKind"));
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
					if (target.isPresent() && !target.equals(feature))
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
				if (!isKind(item.getPackage(), Kind.SHARED))
				{
					return;
				}

				for (Dependency dependency : item.getDirectDependenciesFromSelf())
				{
					JavaPackage target = dependency.getTargetClass().getPackage();
					if (isKind(target, Kind.FEATURE) || isKind(target, Kind.COMPOSITION_ROOT))
					{
						events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
					}
				}
			}
		});

	@ArchTest
	static final ArchRule a_feature_or_a_composition_root_is_imported_only_by_its_nearest_root = classes()
		.should(new ArchCondition<JavaClass>("reach a feature or a composition root only from its nearest root")
		{
			@Override
			public void check(JavaClass item, ConditionEvents events)
			{
				Optional<String> from = unitOf(item).map(JavaPackage::getName);

				for (Dependency dependency : item.getDirectDependenciesFromSelf())
				{
					Optional<JavaPackage> unit = unitOf(dependency.getTargetClass());
					if (unit.isEmpty() || from.equals(unit.map(JavaPackage::getName)))
					{
						continue;
					}

					Optional<JavaPackage> root = nearestRootAbove(unit.get());
					if (root.isEmpty() || !root.get().getName().equals(item.getPackageName()))
					{
						events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
					}
				}
			}
		});

	@ArchTest
	static final ArchRule a_feature_exposes_one_public_type = all(PACKAGES)
		.should(new ArchCondition<JavaPackage>("expose no more than one public type at its root")
		{
			@Override
			public void check(JavaPackage item, ConditionEvents events)
			{
				if (!isKind(item, Kind.FEATURE) || enclosingFeature(item).isPresent())
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
					events.add(SimpleConditionEvent.violated(
						item,
						item.getName() + " exposes " + exposed.size() + " public types: "
							+ String.join(", ", exposed)));
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
				if (isKind(item, Kind.FEATURE))
				{
					return;
				}

				enclosingFeature(item)
					.ifPresent(feature -> events.add(SimpleConditionEvent.violated(
						item,
						item.getName() + " lies inside feature " + feature.getName()
							+ " without declaring itself one")));
			}
		});

	@ArchTest
	static final ArchRule a_feature_is_reached_only_through_its_one_type = classes()
		.should(new ArchCondition<JavaClass>("reach a feature only through its one public type")
		{
			@Override
			public void check(JavaClass item, ConditionEvents events)
			{
				Optional<String> feature = featureOf(item);

				for (Dependency dependency : item.getDirectDependenciesFromSelf())
				{
					JavaClass target = dependency.getTargetClass();
					Optional<String> reached = featureOf(target);
					if (reached.isEmpty() || reached.equals(feature) || isDoorOf(target, reached.get()))
					{
						continue;
					}

					events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
				}
			}
		});

	@ArchTest
	static final ArchRule no_package_holds_a_cycle_with_another = slices().matching(ROOT + ".(**)").should()
		.beFreeOfCycles();

	@ArchTest
	static final ArchRule no_type_is_declared_inside_another = classes()
		.should(new ArchCondition<JavaClass>("be declared in a file of their own")
		{
			@Override
			public void check(JavaClass item, ConditionEvents events)
			{
				if (!item.isNestedClass() || item.isAnonymousClass())
				{
					return;
				}

				item.getEnclosingClass()
					.ifPresent(outer -> events.add(SimpleConditionEvent.violated(
						item, item.getSimpleName() + " is declared inside " + outer.getName())));
			}
		});

	/** The kind a package declares, if it declares one. */
	private static Optional<Kind> kindOf(JavaPackage javaPackage)
	{
		return javaPackage.tryGetAnnotationOfType(PackageKind.class).map(PackageKind::value);
	}

	/** Whether a package declares the given kind. */
	private static boolean isKind(JavaPackage javaPackage, Kind kind)
	{
		return kindOf(javaPackage).filter(kind::equals).isPresent();
	}

	/** Whether a package is the root these rules apply to, or lies inside it. */
	private static boolean isWithinRoot(JavaPackage javaPackage)
	{
		return javaPackage.getName().equals(ROOT) || javaPackage.getName().startsWith(ROOT + ".");
	}

	/**
	 * Names the feature a class belongs to, if it belongs to one. A feature may hold subpackages, so
	 * the feature is the outermost package declaring {@link Kind#FEATURE} at or above the class.
	 */
	private static Optional<String> featureOf(JavaClass item)
	{
		return unitOf(item)
			.filter(javaPackage -> isKind(javaPackage, Kind.FEATURE))
			.map(JavaPackage::getName);
	}

	/**
	 * Names the package a class is reached through - the root of the feature it belongs to, or its
	 * own package when that package is a composition root. A shared package is reached through
	 * nothing.
	 */
	private static Optional<JavaPackage> unitOf(JavaClass item)
	{
		JavaPackage javaPackage = item.getPackage();
		if (isKind(javaPackage, Kind.FEATURE))
		{
			return Optional.of(enclosingFeature(javaPackage).orElse(javaPackage));
		}

		return isKind(javaPackage, Kind.COMPOSITION_ROOT) ? Optional.of(javaPackage) : Optional.empty();
	}

	/** Names the outermost feature enclosing a package, if any feature encloses it. */
	private static Optional<JavaPackage> enclosingFeature(JavaPackage javaPackage)
	{
		return ancestorsOf(javaPackage).stream()
			.filter(ancestor -> isKind(ancestor, Kind.FEATURE))
			.reduce((nearer, further) -> further);
	}

	/** Names the composition root nearest above a package, if one encloses it at any depth. */
	private static Optional<JavaPackage> nearestRootAbove(JavaPackage javaPackage)
	{
		return ancestorsOf(javaPackage).stream()
			.filter(ancestor -> isKind(ancestor, Kind.COMPOSITION_ROOT))
			.findFirst();
	}

	/** The packages above one, nearest first. */
	private static List<JavaPackage> ancestorsOf(JavaPackage javaPackage)
	{
		List<JavaPackage> ancestors = new ArrayList<>();
		for (Optional<JavaPackage> ancestor = javaPackage.getParent(); ancestor
			.isPresent(); ancestor = ancestor.get().getParent())
		{
			ancestors.add(ancestor.get());
		}

		return ancestors;
	}

	/** Whether a class is the one public type a feature exposes. */
	private static boolean isDoorOf(JavaClass item, String feature)
	{
		return item.getPackageName().equals(feature) && isExposed(item);
	}

	/** Whether a class is a public type of its package, rather than a package-private one. */
	private static boolean isExposed(JavaClass item)
	{
		return item.getModifiers().contains(JavaModifier.PUBLIC)
			&& !item.getSimpleName().equals("package-info");
	}
}
