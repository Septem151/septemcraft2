# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

SeptemCraft is a Minecraft **Forge 1.20.1** mod (Java 17), currently a Forge MDK skeleton plus the
package-structure machinery that governs everything added to it, plus a design record in `docs/`.
Essentially all substantive information about the project lives in `docs/`, not in code at this
point in time.

```
src/main/java/io/gifsync/septemcraft
├── SeptemCraftMod            the @Mod entrypoint; holds MODID
├── SeptemCraftConfig         an empty @Mod.EventBusSubscriber placeholder
└── structure                 Kind and @PackageKind - what a package is
src/test/java                 ArchUnit over the structure, and JUnit over the model
src/gametest/java             Forge GameTests; not yet created
```

**Every package carries a `package-info.java` annotated `@PackageKind(Kind.…)`.** A new package
without one fails `./gradlew build` - the kind is what decides where that package may import from.

Nothing outside `io.gifsync.septemcraft` may name a class in it, `SeptemCraftMod.MODID` included:
the mod root is a composition root with no root above it, so the structure rules forbid reaching
into it. A feature needing the mod id needs it promoted to a shared package first.

## Commands

All via the Gradle wrapper (Gradle 8.8, `org.gradle.daemon=false`, `-Xmx3G`). Gradle 8.8 does not
run on this machine's default JDK, so **every command needs
`JAVA_HOME=/usr/lib/jvm/java-17-openjdk`** - without it the wrapper fails before reading the build:

```bash
./gradlew test             # compile checks + ArchUnit + JUnit - the fast loop, seconds
./gradlew spotlessApply    # rewrite every source set in the one format
./gradlew build            # the above, plus compile + jar (jar is finalizedBy reobfJar)
./gradlew runClient        # launch dev client (working dir: ./run, gitignored)
```

Reach for `./gradlew test` while iterating: it carries Error Prone, NullAway, ArchUnit and JUnit
with no `reobfJar` behind it. `build` adds the format check and the jar.

### Tests

Three mechanisms, and they do not overlap:

- **JUnit** (`src/test/java`) tests the model - values and the arithmetic over them, everything that
  needs no world. Tests mirror the package names, so they reach package-private types and nothing is
  made public in order to be tested.
- **ArchUnit** (`src/test/java`) enforces the package structure. Its `DoNotIncludeTests` option keeps
  the model tests out of the structural analysis, so both live in that source set without conflict.
- **Forge GameTests** (`src/gametest/java`) answer what only a world can - placement, adjacency,
  wiring, redstone, failure. A "unit test" that needs a `Level` is a GameTest that has not admitted
  it yet, and nothing structural will stop you writing one.

`./gradlew build` runs the first two and compiles the third. Both run configs set
`forge.enabledGameTestNamespaces=septemcraft`, so any `@GameTestHolder(SeptemCraftMod.MODID)` class
is picked up automatically. Run them from inside a dev client/server with `/test runall`, or
`/test run <namespace>:<name>` for a single test. For a headless run-all-then-exit, uncomment the
`gameTestServer` run config in `build.gradle` (then `./gradlew runGameTestServer`).

### Enforcement

Nothing here warns - each of these fails the build.

- **Spotless** holds every source set to `config/eclipse-format.properties` - the RuneLite
  IntelliJ scheme translated for the Eclipse engine, so the build agrees with the IDE.
  `./gradlew spotlessApply` is the fix.
- **Error Prone** runs as a compiler plugin over every source set, under `-Xlint:all -Werror`.
  `-Xlint` is not optional: javac reports a deprecated call or an unchecked cast as a note that
  `-Werror` cannot see until a lint category asks for it.
- **NullAway** treats `io.gifsync.septemcraft` as non-null by default. Minecraft and Forge are
  unannotated, so a value they hand back is converted where it arrives rather than assumed.
- **ArchUnit** enforces the package structure.
- **CI** runs `./gradlew build` on push and pull request (`.github/workflows/build.yml`).

## Build configuration

`gradle.properties` is the single source of truth for versions and mod metadata. `processResources`
expands `${...}` placeholders into `META-INF/mods.toml` and `pack.mcmeta`, so **never hardcode a
version or mod name in those resource files** - add a property and reference it. The
`replaceProperties` map in `build.gradle` must list any new property before it can be used there.

`mod_id` (`septemcraft`) must stay in sync with `SeptemCraftMod.MODID`; changing one without the
other breaks mod loading silently at the resource level.

Mappings are **Parchment `2023.10.08-1.20.2`** layered over official - parameter names and javadocs
are available on Minecraft classes, unlike plain official mappings.

`src/generated/resources` is already registered as a resource source dir for data generation, but
the `data` run config is commented out in `build.gradle`; uncomment it to add datagen.

## Design docs - read these before writing feature code

- `docs/00-decisions.md` - **settled** decisions only. Its own rule: *"Do not infer a decision from
  silence - if it is not written down, it is not decided."*
- `docs/legacy/01-open-questions.md` - explicitly undecided questions and open topics.
- `docs/03-code-principles.md` - the rules all code follows.
- `docs/04-package-structure.md` - where code goes: the three package kinds and what each may
  import. Enforced by `./gradlew build`, not by review.

Load-bearing decisions that constrain implementation work:

- The mod's first (and currently only) content area is an **electrification module**: converting
  Create rotation to electricity, transmitting it, and converting it back or driving electric-native
  devices. Status is **ideation - no code written yet**. First planned vertical slice: generators.
- **Create 6.0.8 and AE2 15.4.10 are hard dependencies** - no soft-dep branching, no standalone
  mode. They are *not yet declared*; adding them means both a `fg.deobf(...)` dependency in
  `build.gradle` and a `[[dependencies.septemcraft]]` block in `mods.toml`.
- **Electrical quantities**: RPM → volts, total SU → watts, current derived as P/V. "SU" is the
  total stress figure (`impact × RPM`). Watts and total SU are one currency. Transmission loss
  follows from this and scales with current.
- Rotation is the only power source; **batteries are the sole FE boundary** (that is the only place
  Forge Energy may be touched - AE2's Energy Acceptor is the downstream consumer).
- No block-by-block cables, no multiblock formation step, no new ores or worldgen.
- Out of scope: electro-processing recipes, heating devices, trains/electrified rail.

When a task depends on something in `01-open-questions.md`, surface the question rather than
picking an answer.

**This project is early in ideation - `00-decisions.md` is a snapshot, not a contract.** Anything
in it may be reopened, rewritten or removed at any time, including the items summarized above.
Read the doc rather than trusting this summary, and never argue that something is fixed
merely because it is currently written there.
