# Design Decisions - Electrification Module

Status: **ideation**. No code changes yet.
Last updated: 2026-09-17

This file records decisions that are **settled**. Anything not here is either in
`01-open-questions.md` or has not been raised yet. Do not infer a decision from
silence - if it is not written down, it is not decided.

---

## 1. Platform & repository

| Topic     | Decision                      |
|-----------|-------------------------------|
| Minecraft | 1.20.1                        |
| Loader    | Forge 47.4.23                 |
| Create    | **6.0.8**                     |
| AE2       | **15.4.10**                   |
| Java      | 17                            |
| Mappings  | Parchment `2023.10.08-1.20.2` |
| Gradle    | 8.8 wrapper                   |
| Mod id    | `septemcraft`                 |
| Package   | `io.gifsync.septemcraft`      |
| Licence   | GNU GPLv3                     |

**Repository structure.** The electrification system is a **module inside the
SeptemCraft mod**. Other, unrelated "opinionated world" content may live
alongside it later.

## 2. Dependencies

Both Create and AE2 are **hard dependencies**. No fallback paths, no soft-dependency branching,
no "works standalone" mode.

- **Create** - the sole origin of all power, plus the aesthetic and UX idiom.
- **AE2** - supplies materials (sky stone), worldgen (meteorites) and the primary
  downstream consumer (Energy Acceptor, via FE).

## 3. What the mod is for

Two jobs:

1. **Transmission.** Rotation is impractical to move any real distance. Convert
   rotation to electricity, move it across the world, convert it back to rotation
   where the work happens. Wires as base-to-base infrastructure.
2. **Electric-native devices.** Loads that rotation cannot drive: lighting,
   batteries, sensors and instrumentation.

Explicitly **not** a goal: Electro-processing recipes (arc furnaces, electrolysis, induction heating) are
out of scope.

## 4. Tone and audience

- **Aesthetic:** Immersive Engineering - realism in service of how it *looks*.
  Visible infrastructure you are proud of: slung wires, transformers on pads,
  busbars in a plant room, giant transmission poles, etc.
- **Integration feel:** Seamless Create and AE2 integration; animated, taught by
  Ponder rather than by a wiki.
- **Audience:** a personal world, but engineered and documented to
  public-release quality. No compat matrix obligation, no support burden, but no
  excuse for sloppiness either.

## 5. Energy flow - the spine

```
Create rotation  →  generator  →  electrical grid  →  battery  →  FE  →  AE2 Energy Acceptor
                                        │
                                        ├→  motor  →  Create rotation
                                        ├→  lighting
                                        └→  sensors / signalling
```

- **Rotation is the only source of power.** No standalone generators (solar, wind,
  steam-independent, fuel burners), no exotic sources (nuclear, RTG, geothermal).
  Everything upstream of the generator is Create's problem.
- **Batteries are the sole FE boundary.** Nothing else in the mod speaks FE. A
  battery collects generated electricity and exposes it as FE, which is how AE2
  gets powered. Internally the grid uses the mod's own electrical quantities.

## 6. Current types

**Both AC and DC exist from the start.** The player
chooses per circuit, and conversion between AC and DC is expected to exist.

## 7. Wiring - three form factors

Deliberately **no block-by-block cable/conduit**. Three distinct forms, each with
a job:

| Form                          | Role                                                                                                                   |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------|
| **Catenary wires** (IE-style) | Long-haul outdoor transmission. Slung between insulators on poles, sagging, crossing terrain without occupying blocks. |
| **Busbars**                   | High-current rigid distribution inside a plant room - the spine that breakers, meters and machine feeds tap off.       |
| **Flat surface wiring**       | Thin conduit hugging walls, floors and ceilings for interior runs: lamps, switches, the "wire your house" layer.       |

## 8. Multiblocks

**Composed of working parts - no formation step.** There is no "build the
pattern and hit it with a hammer" moment and no controller-plus-structure-check.
A transformer is a core, plus coils, plus whatever else, each a real block doing a
real job, working because the parts are adjacent and correctly wired.

## 9. Materials

**No new ores and no new worldgen.** Materials come from Create, vanilla, and AE2.

- **Magnetic material: AE2 sky stone**, processed through Create's crushing wheels.
  Chosen because AE2's meteorites already provide a findable resource with a real
  exploration loop, Create already provides the processing verb, and "magnetic rock
  from space" justifies itself.
- **Conductor: copper**, from vanilla, processed through Create's recipes to produce wires
  and other crafting items.

**Design emphasis:** *energy production requires resource gathering.* Generators
are built from copper windings, magnetic shafts, and the cost of
building them is a central part of the experience - not an afterthought to a
balance number.

## 10. Transmission range

**Long-distance, same-dimension, unloaded-chunk aware.** Hundreds to thousands of
blocks across the Overworld. Critically, a line must keep working when the middle
of it is not loaded - it behaves as a logical link rather than requiring every
chunk in between to tick. No cross-dimension power. Power decreases over distance unless
using high voltage AC, like real life.

## 11. Devices

In scope:

- **Motors** - electricity back to Create rotation. The other end of the bridge.
- **Lighting** - a family, not one lamp block. Bulbs, floodlights,
  streetlamps, indicator lights, with behavior driven by actual electrical state.
- **Signaling and sensors** - meters, gauges, current sensors, thermostats, limit
  switches. The instrumentation layer that makes a grid legible.
- **Batteries** - storage, and the FE boundary.

Out of scope: heating and electro-processing devices.

## 12. Control

**Two bridged layers.**

- A native **control circuit** layer (switches, relays, contactors) at low voltage,
  commanding the **power circuit** - real switchgear behavior, where a breaker is
  something the control circuit can trip.
- **Redstone converters** in both directions, so redstone can read and drive the
  control layer and vice versa.

## 13. Create integration surface

In scope:

- **Engineer's Goggles overlay** - electrical state shown in Create's own overlay
  idiom.
- **Ponder scenes** - every block taught by an animated scene.
- **Works on contraptions** - wiring and devices survive assembly onto a moving
  contraption.

Out of scope: **trains** - no electrified rail, pantographs, overhead line, or
battery locomotives.

## 14. First vertical slice

**Generators.**
