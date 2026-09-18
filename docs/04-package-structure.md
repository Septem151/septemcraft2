# Package Structure

## The tree

```
io.gifsync.septemcraft          mod root - entrypoint, config, datagen
├── structure                   the kind annotation
└── <module>                    module root
    ├── <shared>                the model
    └── <feature>               the machines
```

The module is a package level of its own. The first and only module is `electrification`.
The mod root and every module root is a composition root: the mod root assembles the modules, a
module root assembles its features.

## The three kinds

Every package declares its kind, and the kind is what says where it may import from.

| Kind                 | Imports                                      | Imported by              |
|----------------------|----------------------------------------------|--------------------------|
| **Shared**           | shared packages only                         | anything                 |
| **Feature**          | shared packages; never a feature, either way | a composition root only  |
| **Composition root** | its features and shared packages             | a root containing it     |

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
- **A package inside a feature exposes one public type**, holding that package's registry objects.
  Everything else is package-private.
- **Client-only code is a `client` subpackage** of the feature it draws.
- **A package inside a feature is part of that feature.** A feature may hold subpackages; they
  are the feature, and nothing outside it reaches into them.
- **Whatever enumerates features is a composition root** - registration, Ponder, client setup,
  datagen.
- **GameTests never ship in the jar.** They live in `src/gametest/java`, mirroring the package
  names so they keep package-private access to what they test.

## Enforcement

`./gradlew build` fails on a violation. The rules are ArchUnit, in `src/test/java`:

1. Every package declares its kind.
2. A feature depends on no other feature.
3. A shared package depends only on shared packages.
4. A composition root is imported only by a composition root containing it.
5. A package in a feature exposes one public type.
6. A package inside a feature declares itself part of that feature.
7. Client-only code lives in a `client` package - `net.minecraft.client` and
   `net.minecraftforge.client` are reachable from nowhere else.

`src/gametest/java` is its own source set, compiled by `./gradlew build` and excluded from these
rules.