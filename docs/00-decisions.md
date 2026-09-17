# Design Decisions - Electrification Module

Status: **ideation**. No code changes yet.
Last updated: 2026-09-17

This file records decisions that are **settled**. Anything not here is either in
`01-open-questions.md` or has not been raised yet. Do not infer a decision from
silence - if it is not written down, it is not decided.

Settled does not mean permanent. The project is early in ideation: any entry here
may be reopened, rewritten or removed at any time. Refer to sections by name rather
than by number - they get reordered, and numbers cited elsewhere go stale.

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

## 6. Electrical quantities

**RPM → volts, total SU → watts, current derived as P/V.**

| Create quantity           | Electrical        | Status  |
|---------------------------|-------------------|---------|
| Shaft speed (RPM)         | **Voltage**       | primary |
| Total network stress (su) | **Watts**         | primary |
| -                         | **Current** = P/V | derived |

"SU" here is the **total stress** figure: Create's `impact × RPM` product, the number
the goggles show against capacity.

**Current is a derived readout, not a primary quantity.** Nameplates, tooltips and
Ponder scenes are written in volts and watts - `1,024 W @ 128 V`, the way a real
genset is rated - because that is the form Create's goggles have already taught.
Protection hardware is the exception: ampacities and breaker ratings are given in amps,
because current is the quantity they act on.

Consequences:

- **Watts and total SU are one currency.** Power is conserved across the generator.
  There is no separate electrical economy to balance against Create's.
- **Equal Create cost buys equal power.** One generator geared to 128 RPM and four
  of the same generator at 32 RPM draw the same total stress and produce the same
  wattage. They differ only in V and I.
- **Gearing trades voltage for current at constant power.** Since
  `I = P/V = (impact × RPM) / RPM`, current tracks the generator's `su/RPM` impact
  rating and is independent of shaft speed. A transformer is a gearbox; that is the
  intended teaching hook.
- **Every device has a voltage rating.** Below it the device does not run; from there up
  to the ceiling of its voltage class it runs normally, and above that ceiling it fails
  per *Failure model*. Because RPM *is* voltage, "this lamp needs 64 V" is the same
  statement as Create's "this machine needs 32 RPM".

## 7. Current types - AC and DC, with distinct jobs

**Both AC and DC exist from the start, and transformers work only on AC.** That
constraint is what gives each current type its job.

- **AC is the grid.** Generation is AC - an alternator produces it natively. Any
  circuit whose voltage has to change must be AC, so every long haul is AC - stepped
  up at the source, stepped down at the far end. See *Transmission range* for why a
  long haul wants high voltage at all.
- **DC is the local domain.** Battery internals, the control circuits of *Control*,
  and sensors and instrumentation are DC: a local circuit needs a specific voltage,
  but never needs to change it again once it has it. Stepping happens on the AC side,
  ahead of the rectifier - a low-voltage control circuit hanging off a high-voltage
  line is transformed down as AC and then rectified - so the DC domain sits **downstream** of a transformer.
  Every DC circuit begins at a rectifier or at a battery.
- **Rectifiers and inverters** sit at the boundaries between the two, and each loses a
  small fixed fraction of the power crossing it.

The choice is made by the job: a run that needs stepping is AC, and a low-voltage
local circuit has no reason to be.

## 8. Wiring - three form factors

Three distinct forms, each with a job:

| Form                          | Role                                                                                                                   |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------|
| **Catenary wires** (IE-style) | Long-haul outdoor transmission. Slung between insulators on poles, sagging, crossing terrain without occupying blocks. |
| **Busbars**                   | High-current rigid distribution inside a plant room - the spine that breakers, meters and machine feeds tap off.       |
| **Flat surface wiring**       | Thin conduit hugging walls, floors and ceilings for interior runs: lamps, switches, the "wire your house" layer.       |

**Bare or insulated is a property of the form, and it is about contact, not capacity.**
Catenary wire is bare and kept out of reach on poles. Busbars are bare, which is why they
belong in a plant room rather than a corridor. Surface wiring is jacketed, because it runs
where people walk past it. See *Shock* for what a bare conductor costs to touch, and
*Failure model* for why a jacket is not what lets a conductor carry a voltage.

## 9. Multiblocks

**Composed of working parts - no formation step.** There is no "build the
pattern and hit it with a hammer" moment and no controller-plus-structure-check.
A transformer is a core, plus coils, plus whatever else, each a real block doing a
real job, working because the parts are adjacent and correctly wired.

## 10. Materials

**No new ores and no new worldgen.** Materials come from Create, vanilla, and AE2.

- **Magnetic material: AE2 sky stone**, processed through Create's crushing wheels.
  Chosen because AE2's meteorites already provide a findable resource with a real
  exploration loop, Create already provides the processing verb, and "magnetic rock
  from space" justifies itself.

  Sky stone is crushed to dust, the dust is mixed with iron and heated into a
  magnetic alloy ingot, and the ingot is pressed into a magnet. Each alternator
  segment carries one.

- **Conductor: copper**, from vanilla, drawn into wire in a single step. Wire is the
  branch point of the chain: strung as-is for catenary, wound into the coils of
  alternator segments, or jacketed with rubber to make insulated wire for interior runs.

- **Insulator: clay, fired into porcelain.** Clay is dug from riverbeds and lush caves
  and fired through Create. Porcelain is what carries voltage class per *Failure model*,
  so stepping up class is a new string of discs.

- **Jacket: rubber, from dandelion latex.** Dandelions are crushed for latex and the latex
  is vulcanized into sheet, which wraps wire for interior runs. The jacket is touch
  protection per *Shock*; it has no bearing on the voltage a conductor can carry.

**Design emphasis:** *energy production requires resource gathering.* Generators
are built from copper windings and magnets, and the cost of building them is a central
part of the experience - not an afterthought to a balance number.

## 11. Transmission range

**Long-distance, same-dimension, unloaded-chunk aware.** Hundreds to thousands of
blocks across the Overworld. Critically, a line must keep working when the middle
of it is not loaded - it behaves as a logical link rather than requiring every
chunk in between to tick. No cross-dimension power.

**Distance causes loss, and loss depends on current** - a consequence of the
quantity mapping. Stepping up the voltage is therefore how a long haul is run.

## 12. Devices

In scope:

- **Motors** - electricity back to Create rotation. The other end of the bridge.
- **Lighting** - a family, not one lamp block. Bulbs, floodlights,
  streetlamps, indicator lights, with behavior driven by actual electrical state.
- **Signaling and sensors** - meters, gauges, current sensors, thermostats, limit
  switches. The instrumentation layer that makes a grid legible.
- **Batteries** - storage, and the FE boundary.

Out of scope: heating and electro-processing devices.

## 13. Control

**Two bridged layers.**

- A native **control circuit** layer (switches, relays, contactors) at low voltage,
  commanding the **power circuit** - real switchgear behavior, where a breaker is
  something the control circuit can trip.
- **Redstone converters** in both directions, so redstone can read and drive the
  control layer and vice versa.

## 14. Failure model

**Nothing fails because of power.** Damage is always caused by current or by voltage,
and those two fail in different shapes.

| Cause       | Physically        | Shape of failure                          | Protection                        |
|-------------|-------------------|-------------------------------------------|-----------------------------------|
| **Current** | heat              | cumulative, builds and drains             | breakers, sized in amps           |
| **Voltage** | dielectric stress | faster the further over, never cumulative | arresters, and relays on breakers |
| **Power**   | the work budget   | no damage - the network stalls            | generation capacity               |

Current damage is earned slowly and can be caught while it is happening. Voltage damage
is much faster and at the extreme leaves no time at all, so it is met by clamping the
voltage rather than by opening the circuit. Both are covered under *Protection*.

Power is not a damage cause at all. Watts and total SU being one currency per
*Electrical quantities*, demand beyond what the generators supply is Create's own stress
overload - the rotational network stalls and everything on it stops together. Nothing is
harmed, and the answer is more generation.

### Current - the thermal chain

Every conductor carries an **ampacity**: catenary wire, busbars, surface wiring,
generator coils, transformer windings, and contacts. Each holds a heat state that rises
while current is over ampacity and drains while it is not. The rate rises with how far
over the rating the current is, so a larger overload fails sooner and one curve sets
every threshold.

| State       | Condition                                    | Effect                                          |
|-------------|----------------------------------------------|-------------------------------------------------|
| **Hot**     | over ampacity                                | visible, audible, readable by sensors; no damage |
| **Trip**    | heat reaching the limit, protection present  | the breaker opens; nothing is damaged            |
| **Burnout** | heat reaching the limit, unprotected         | the component is destroyed                       |

Under ampacity nothing heats and nothing is at risk; transmission loss still scales with
current per *Transmission range*.

A **dead short** is instantaneous. Current is unbounded, no heat accumulates first, and
the result is an arc.

**Burnout leaves wreckage in place.** A block becomes a burnt variant that stays where it
stood and is recyclable - crushing or melting it returns a fraction of its copper, and
its magnets are lost. A catenary wire snaps and drops nothing; its poles and insulators
survive.

### Voltage - the dielectric wall

Every component carries a **voltage class, and the class ceiling is its withstand
voltage**. Between its own operating rating and that ceiling a device runs normally -
voltage inside the class is free. Above the ceiling it breaks down, and how quickly
follows how far over it is: a modest over-voltage takes a moment, a gross one leaves no
time at all. That is the same shape as the thermal curve on the current side, with the one
difference that matters - **dielectric stress does not accumulate.** The clock resets the
instant voltage drops, and a component that has ridden out an over-voltage is no weaker
for it.

**Three classes: LV, MV, HV.** Where the boundaries sit is open.

**Class is carried by insulators, never by a conductor's jacket.** What is rated is
whatever holds a conductor away from grounded structure - the insulator on a catenary
pole, the standoff a busbar sits on. Air does the rest, which is why a bare span can be
the highest-voltage thing in the world. A higher-class insulator is physically a bigger
one - the same material in greater quantity, stepped through Create's mechanical
crafting - and that is legible from the ground: you can read a line's class off its poles.

**Over-voltage on a span is a flashover.** The arc tracks across the insulator to the
pole. The insulator shatters and the span it held drops, because nothing is carrying it
any more; the conductor itself is undamaged. That inverts the over-current case, where the
wire snaps and the poles and insulators survive, so the two failures stay distinguishable
on sight. A flashover is a conductor-to-ground fault, so it is also a short, and its arc
is the one described in *Arcs*.

**A span's class is that of its weakest insulator.** Stepping a line-up means walking its
length and replacing every insulator on it. Miss one, and that pole is where the line
fails.

**Under-voltage damages nothing.** Above its operating rating, a device meets a sagging
voltage by running slower and nothing else: a Create load's impact is rated in su/RPM, so
its power demand falls with speed, and a sag lowers `P` and `V` together while leaving
`I = P/V` unchanged. Below the rating the device cuts out, per *Electrical quantities*,
and sits there unharmed. A brownout costs speed and then motion; it never costs hardware.

### Arcs

An arc is the terminal event of both causes - a dead short on the current side, a
breakdown on the voltage side.

An arc flashes, cracks, sets fire to nearby flammables, and damages and blinds nearby
entities. The faulted component is destroyed outright and drops nothing. **Terrain is
not damaged.** The exception is a **battery, which explodes for real**, because its
stored energy has somewhere to go. Violence scales with what was feeding the fault.

### Protection

**Circuit breakers are multiblocks built to open a circuit.** Per *Multiblocks* they are
composed of working parts with no formation step, and per *Control* a breaker is something
the control circuit can trip. What decides when it trips is the sensing driving it, and
there is one for each cause:

- **Over-current.** The breaker opens before heat reaches the limit, so a fault stops at
  the trip state instead of reaching burnout. Sized in amps.
- **Over-voltage.** A relay watching line voltage trips the breaker. This catches the
  modest over-voltage that takes a moment to break down - a generator geared too fast, or
  a circuit run into the wrong class. It cannot catch a gross one, which arcs before
  anything mechanical can move.

**Surge arresters** stand in front of what a breaker cannot reach. An arrester clamps:
above its rating it conducts the excess to ground at the speed of the fault instead of
waiting for a mechanism, which is exactly why it protects where opening a circuit cannot.
It pays by absorbing that energy itself, and a large enough surge destroys it - which is
the point. An arrester is cheap and a transformer is not, and a spent one is visible on
the pole that took the hit.

## 15. Shock

**A bare energized conductor is dangerous to touch.** That hazard is the whole reason
insulation exists in this mod; per *Failure model* a jacket has nothing to do with voltage
class.

Catenary wire and busbars are bare, so both are live whenever the grid is. Surface wiring
is jacketed and safe to be around. The intent is that bare conductors live where people
are not: catenary slung overhead out of reach, busbars in a plant room entered
deliberately rather than a corridor walked down.

As with arcs, severity follows the circuit - what a contact costs scales with what is
behind it.

## 16. Create integration surface

In scope:

- **Engineer's Goggles overlay** - electrical state shown in Create's own overlay
  idiom.
- **Ponder scenes** - every block taught by an animated scene.
- **Works on contraptions** - wiring and devices survive assembly onto a moving
  contraption.

Out of scope: **trains** - no electrified rail, pantographs, overhead line, or
battery locomotives.

## 17. First vertical slice - generators

**Generators**, built as an **axial stack of alternator segments on a Create shaft**.

```
                    side view

 shaft in                                        out
    ═══▶ [SEG] [SEG] [SEG] [SEG] [TERMINAL] ───▶ to the grid
          └──── coils, in series ────┘
```

A generator is a line of identical segments threaded on one shaft, each a real block
carrying magnet and windings, **capped at the far end by a terminal**. Per *Multiblocks* there is no formation step:
segments count because they are in line on
the same shaft, and a segment placed off the line is simply a segment doing nothing.
Stack length is how a generator grows - physically long, industrial, and at home in a
plant room.

**Electricity leaves at the terminal as AC, and nowhere else.** The coils run in series down
the stack and converge there; a segment does not have output of its own, so there is no
tapping into the middle of a machine.

**The scaling law follows from *Electrical quantities*.** Each segment contributes a
fixed stress impact (su/RPM). Since `I = P/V = (impact ×
RPM) / RPM`, that is exactly the definition of current:

| Physical fact         | Electrical consequence                      |
|-----------------------|---------------------------------------------|
| Segments in the stack | **Current** - each segment adds a fixed `I` |
| Shaft speed           | **Voltage**                                 |
| The two multiplied    | **Watts**, equal to the stress drawn        |

So a longer stack is a higher-current machine, gearing the shaft faster is a
higher-voltage one, and the stress Create sees is the wattage produced - power is
conserved across the generator. The two ways to build a bigger
generator are to lengthen the stack or to gear it up, and they are not interchangeable:
they land in different places on the transmission-loss curve.
