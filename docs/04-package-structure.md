# Package Structure

## The tree

```
io.gifsync.septemcraft          mod root - entrypoint, config, datagen
├── structure                   the kind annotation
├── namespace                   the mod id
├── processing                  Create processing recipes, as values
└── <module>                    module root
    ├── <shared>                the model
    └── <feature>               the machines
```

A shared package may sit at the mod root as well as inside a module. `structure`, `namespace` and
`processing` are shared by every module, so they sit above all of them; a model only one module
needs belongs inside that module.

The module is a package level of its own. The first and only module is `electrification`.
The mod root and every module root is a composition root: the mod root assembles the modules, a
module root assembles its features.

## The three kinds

Every package declares its kind, and the kind is what says where it may import from.

| Kind                 | Imports                                               | Imported by      |
|----------------------|-------------------------------------------------------|------------------|
| **Shared**           | never a feature or a composition root                 | anything         |
| **Feature**          | shared packages; never a feature, either way          | the nearest root |
| **Composition root** | its features, a root it contains, and shared packages | the nearest root |

Shared is the model; a feature is the machine.

A package declares itself in its `package-info.java`:

```java
@PackageKind(Kind.FEATURE)
package io.gifsync.septemcraft.electrification.transformer;
```

## Rules

- **A feature is a vertical slice** - its blocks, behaviour and rendering together.
- **Two features never meet.** What passes between them is promoted to a shared package, and
  neither learns the other exists.
- **A composition root reaches only its own features** - it may depend only on a feature whose
  nearest enclosing composition root is itself. The mod root reaches a module's features through
  the module root, never directly.
- **A feature exposes one public type**, holding its registry objects. Nothing outside the feature
  names anything else in it.
- **A package inside a feature is part of that feature.** A feature may hold subpackages; they
  are the feature, and nothing outside it reaches into them, so what they expose is unconstrained.
- **Whatever enumerates features is a composition root** - registration, Ponder, client setup,
  datagen.
- **GameTests never ship in the jar.** They live in `src/gametest/java`, mirroring the package
  names so they keep package-private access to what they test.

## What the rules force on registration

The mod root is a composition root with no root above it, so **nothing may import it**, the `@Mod`
class included. That is why the mod id does not live there. Registration has a shape it cannot
avoid:

- **The mod id lives in a shared package.** `Namespace.ID`, imported by anything that needs to
  name what it registers.
- **A feature holds its own `DeferredRegister`** and exposes its registry objects through the one
  public type its root package is allowed. It is constructed with the mod event bus rather than
  registering itself statically.
- **The mod root reaches a feature only through its module root.** `SeptemCraftMod` builds
  `Electrification`, which builds the features; the tab and the data providers ask the module,
  never the feature.
- **Whatever enumerates features is a composition root**, so the creative tab and data generation
  live at the mod root even though what fills them belongs to the features.

## Enforcement

`./gradlew build` fails on a violation. The rules are ArchUnit, in `src/test/java`:

1. Every package declares its kind.
2. A feature depends on no other feature.
3. A shared package depends on neither a feature nor a composition root.
4. A feature or a composition root is imported only by the composition root nearest enclosing it.
5. A feature root exposes one public type.
6. A package inside a feature declares itself part of that feature.
7. No package holds a cycle with another.
8. A feature is reached only through its one public type.
9. No type is declared inside another.

`src/gametest/java` is its own source set, compiled by `./gradlew build` and excluded from these
rules.
