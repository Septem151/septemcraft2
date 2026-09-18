# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

SeptemCraft is a Minecraft **Forge 1.20.1** mod (Java 17), currently a Forge MDK
skeleton - `SeptemCraftMod` (the `@Mod` entrypoint) and an empty `SeptemCraftConfig` - plus a
design record in `docs/`. Essentially all substantive information about the project lives in
`docs/`, not in code at this point in time.

## Commands

All via the Gradle wrapper (Gradle 8.8, `org.gradle.daemon=false`, `-Xmx3G`):

```bash
./gradlew build            # compile + jar (jar is finalizedBy reobfJar)
./gradlew runClient        # launch dev client (working dir: ./run, gitignored)
```

### Tests

There is no `src/test` and no JUnit dependency. The test mechanism this project is wired for is **Forge GameTests**:
both run configs set `forge.enabledGameTestNamespaces=septemcraft`, so any
`@GameTestHolder(SeptemCraftMod.MODID)` class is picked up automatically. Run them from inside a
dev client/server with `/test runall`, or `/test run <namespace>:<name>` for a single test.
For a headless run-all-then-exit, uncomment the `gameTestServer` run config in `build.gradle`
(then `./gradlew runGameTestServer`).

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
- `docs/01-open-questions.md` - explicitly undecided questions and open topics.
- `docs/03-code-principles.md` - the nine rules all code follows. Cite by number in review.

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
