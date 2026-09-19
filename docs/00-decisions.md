# Design Decisions - Electrification Module

Status: **ideation**, with the materials chain built. Everything else is unwritten.
Last updated: 2026-09-19

This file records decisions that are **settled**. Anything not here is either in
`legacy/01-open-questions.md` or has not been raised yet. Do not infer a decision from
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
| Mappings  | Parchment `2023.09.03-1.20.1` |
| Gradle    | 8.8 wrapper                   |
| Mod id    | `septemcraft`                 |
| Package   | `io.gifsync.septemcraft`      |
| Licence   | GNU GPLv3                     |

The Parchment export is the one built for 1.20.1. An export built for another version applied to
1.20.1 leaves methods unmapped in the Minecraft jar while dependency jars are remapped in full, and
a dependency implementing one of them fails at runtime rather than at build time.

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

**RPM → volts, total SU → watts, and current is what the circuit solves for.**

| Create quantity            | Electrical      | Status                       |
|----------------------------|-----------------|------------------------------|
| Shaft speed (RPM)          | **Voltage**     | primary                      |
| Total network stress (su)  | **Watts**       | primary                      |
| Stress impact (su/RPM)     | **Current**     | solved, per machine, per tick |

"SU" here is the **total stress** figure: Create's `impact × RPM` product, the number
the goggles show against capacity. At one volt per RPM and one watt per stress unit,
**one ampere is one su/RPM exactly**, and one ohm is one RPM per su/RPM.

**Current is solved for, not declared.** Every energised circuit is solved as a circuit -
sources, resistances and loads together - and a machine's current is the answer that solve
gives it. Its stress impact is that answer handed back to Create. Nothing about a machine's
construction sets its current; its copper sets the current it can *survive*. A generator
turning into an open circuit costs almost nothing to turn, and the same generator into a
full load costs exactly its wattage.

**A fixed stress impact is a constant-current device, not a resistor.** A resistor's impact
rises with the speed it sees, because it draws `V/R` at `V` volts and so eats `V² / R` of
stress. Both exist, and they are not the same machine.

**Power can flow backwards.** Two generators on one bus settle at one bus voltage, and a
machine whose shaft is geared slower than that voltage absorbs current instead of supplying
it - Create sees it adding capacity to its kinetic network rather than drawing from it.
Reverse power is an outcome of the model, never an error to be clamped away.

Nameplates, tooltips and Ponder scenes are written in volts and watts - `1,024 W @ 128 V`,
the way a real genset is rated - because that is the form Create's goggles have already
taught. Protection hardware is the exception: ampacities and breaker ratings are given in
amps, because current is the quantity they act on.

Consequences:

- **Watts and total SU are one currency.** Power is conserved across the generator.
  There is no separate electrical economy to balance against Create's.
- **Equal Create cost buys equal power.** One generator geared to 128 RPM and four of the
  same generator at 32 RPM carrying the same load draw the same total stress and deliver
  the same wattage. They differ only in V and I.
- **Gearing trades voltage for current at constant power.** A machine's rated current
  follows its copper and is independent of shaft speed, so gearing it up buys volts at the
  same amps. A transformer is a gearbox; that is the intended teaching hook.
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
A machine is an arrangement of real blocks each doing a real job, working because the
parts are adjacent and correctly wired.

**Parts are blocks; consumables and fittings are contents.** A block that needs filling
is filled by interacting with it, not by placing something next to it. Coils and oil go
*into* a transformer block rather than beside it, and what a block holds is part of
what it is - a coil of six windings is a different device from the same block with two.

## 10. Materials

**No new ores and no new worldgen.** Materials come from Create, vanilla, and AE2.

- **Magnetic material: AE2 sky stone**, processed through Create's crushing wheels.
  Chosen because AE2's meteorites already provide a findable resource with a real
  exploration loop, Create already provides the processing verb, and "magnetic rock
  from space" justifies itself.

  Sky stone is crushed to dust, the dust is mixed with iron and heated into a
  magnetic alloy ingot, and the ingot is pressed into a magnet. Each alternator
  segment carries one. The chain is settled end to end:

  | Step  | Machine                   | In                             | Out                                  |
  |-------|---------------------------|--------------------------------|--------------------------------------|
  | Crush | Crushing wheels           | 1 sky stone block              | 1 sky stone dust, + the block at 25% |
  | Alloy | Basin over a Blaze Burner | 1 sky stone dust + 3 iron ingots | 2 magnetic alloy ingots            |
  | Press | Mechanical press          | 1 magnetic alloy ingot         | 1 magnet                             |

  **The dust is AE2's own `ae2:sky_dust`**, not a dust of the mod's own. The mod's chain begins
  at the alloy, AE2's grindstone stays a slower route to the same item, and what crushing wheels
  buy is the quarter chance of handing the block back whole.

  **The alloy is iron-heavy.** Sky stone is the scarce half, so one dust stretched across three
  iron makes a meteorite a supply of alloy rather than an ingot-for-ingot trade, and puts the
  volume cost on an iron industry.

  **The press reads a tag.** The ingot carries `forge:ingots/magnetic_alloy` and the pressing
  recipe names that tag rather than the item, so a second route to the same alloy substitutes
  without touching the recipe.

- **Conductor: copper**, from vanilla, drawn into wire in a single step. Wire is the
  branch point of the chain: strung as-is for catenary, wound into the coils of
  alternator segments, or jacketed with rubber to make insulated wire for interior runs.

- **Insulator: clay, fired into porcelain.** Clay is dug from riverbeds and lush caves
  and fired through Create. Porcelain is what carries voltage class per *Failure model*,
  so stepping up class is a new string of discs.

- **Jacket: rubber, from dandelion latex.** Dandelions are crushed for latex and the latex
  is vulcanized into sheet, which wraps wire for interior runs. The jacket is touch
  protection per *Shock*; it has no bearing on the voltage a conductor can carry.

- **Coolant: oil.** Oil fills transformer coils and carries heat out of them per
  *Transformers*. Where oil comes from is open.

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

## 12. Transformers

### Construction

**A transformer block holds one rod, and windings go onto it.** The block is placed bare
and wound by hand, one winding at a time. A rod with its windings on it is a **coil**, and
the winding count is what a ratio is made of.

**A transformer is two coils.** The smallest is 2x1 - two blocks side by side, each one
tall, each holding a coil. Per *Multiblocks* there is no formation step: they are a
transformer because they are adjacent. **Both coils must carry at least one winding**; a
bare rod on either side is not a transformer.

**Stacking blocks makes a coil taller**, lengthening its rod and giving it room for more
windings. A transformer is two coils of equal height; where they differ it is the height of
the shorter, and the blocks above that are idle.

**Bushings are blocks, and a circuit needs two of them.** A bushing is placed on a top edge
of the transformer and is where a conductor lands. Every circuit is two conductors, so each
coil takes two bushings - **four on a two-coil transformer**: two for the circuit feeding
the primary, two for the circuit the secondary drives. On a taller transformer they sit on
the top block.

**A bushing carries voltage class**, being the insulator that holds a conductor off the
transformer, per *Failure model*. Re-classing a transformer is replacing bushings, as
stepping a line up is walking it replacing insulators, and **a transformer's class is that
of its weakest bushing** - the same rule a span has. A taller porcelain stack on the high
side is readable from the ground.

**Primary and secondary are symmetric.** Whichever pair of bushings is fed is the input;
step-up and step-down are the same hardware wired the other way round.

### The two knobs

**Windings are volts.** A winding is worth a fixed number of volts, and a coil's voltage
rating is its winding count times that constant. The ratio is not a property of its own: it
is the two ratings against each other. So a transformer is read off a nameplate as
`128 V ⇄ 256 V` rather than as 1:2, and two transformers of the same ratio are different
machines - `32 V ⇄ 64 V` and `128 V ⇄ 256 V` are both 1:2, and neither can do the other's
job.

**A coil's winding rating is a ceiling, not a floor** - the inverse of the device rating in
*Electrical quantities*, because a coil is not a load. Fed under its rating a transformer
works and the output scales down with the ratio. Fed over it the core saturates:
magnetizing current climbs steeply and the coil heats. That is current damage, so it runs
the thermal chain of *Failure model* rather than the dielectric wall.

**Blocks are watts.** A block contributes one rod, a fixed budget of copper shared among
the windings on it, so a coil's ampacity is that budget times its block count over its
winding count. Multiplied by the voltage rating the winding count cancels, and the block
count is all that is left:

```
  V = windings x volts-per-winding
  I = copper-budget x blocks / windings
  P = V x I = volts-per-winding x copper-budget x blocks
```

A transformer's wattage is therefore its size and nothing else. Winding it for high voltage
buys volts and spends amps, winding it low does the reverse, and the watts do not move.
**Windings place a transformer on the voltage scale; blocks decide how much power it
moves.**

**High voltage is consequently physically large.** A high-class coil needs many windings,
and many windings need many blocks to carry any current at all. A substation's class is
legible from its silhouette.

### Arrangements

Three, separated by how many coils they carry and how the line meets them:

| | Coils | The line | Produces |
|---|---|---|---|
| **Power transformer** | two | terminates into the primary | power, at a new voltage |
| **Voltage transformer** | two | continues past; a branch taps it | a proportional voltage - a reading |
| **Current transformer** | one | passes through the core | a proportional current - a reading |

**A power transformer and a voltage transformer are the same hardware**, and the wiring is
the entire difference. A power transformer's primary is in series: the circuit ends at its
bushings and everything it carries crosses the core. A voltage transformer's primary sits
across a circuit that carries on past, and only a trickle diverts through it. The block has
no mode to set, so a voltage transformer is wound to the line it watches and a high-class
one is correspondingly large.

```
  power transformer                 voltage transformer
  the circuit terminates at it      the circuit carries on; a branch taps it

  ════╗                             ════╦══════════════▶
  ════╣                             ════╬══════════════▶
   ┌──╨──┐                           ┌──╨──┐
   │  T  │                           │  T  │
   └──╥──┘                           └──╥──┘
      ╠════▶ power                      ╠════▶ reading
      ╚════▶                            ╚════▶
```

Wiring a voltage transformer in series is a real mistake with a real consequence: a coil
sized for a reading, placed in a power path, runs the thermal chain and burns out.

**A current transformer is a single block**, its rod closed into a loop that a conductor
threads. It is a different construction rather than a different wiring - it carries one
coil because the conductor through it is already the primary, a single turn, so the coil's
winding count is its ratio against that one. It takes **a bushing where the conductor
enters and one where it leaves** - one conductor passing through, not a circuit's pair,
since a core around both would see them cancel - and **a tap** for the reading. The line is
not broken, not diverted, and loses nothing.

```
  current transformer
  one conductor threads the core

    bushing      ┌──────┐      bushing
  ═════╤═════════╪══════╪═════════╤═══▶
                 └──┬───┘
                    ▼ tap
```

### Oil and loss

**Oil is cooling, and only overload consumes it.** Coils are filled with oil as well as
windings. At or under ampacity the oil is never touched; over it the oil boils off as it
carries heat out of the core, and a dry coil heats far faster. The escalation is overload,
boil-off, dry, burnout - a well-built transformer never asks for a refill and an abused one
does.

**A transformer loses a small fixed fraction of the power crossing it**, less than a
rectifier or an inverter loses, so stepping up for a haul pays over any distance worth
stepping up for. The loss is a deduction and not heat: per *Failure model* nothing under
ampacity heats.

**Transformers are AC-only** per *Current types*, the instrument arrangements included.

## 13. Devices

In scope:

- **Motors** - electricity back to Create rotation. The other end of the bridge.
- **Lighting** - a family, not one lamp block. Bulbs, floodlights,
  streetlamps, indicator lights, with behavior driven by actual electrical state.
- **Signaling and sensors** - meters, gauges, current sensors, thermostats, limit
  switches. The instrumentation layer that makes a grid legible.
- **Batteries** - storage, and the FE boundary.

Out of scope: heating and electro-processing devices.

**Instruments read a tap, they do not touch the line.** The instrument transformers of
*Transformers* are the measurement primitive: one tap on a conductor, read over control
wiring at low voltage by anything that wants the number. A relay, a meter, a gauge and a
redstone converter on the same tap all see the same reading, and the tap is placed once.

```
  ═══════[CT]═══════════
           |
           |--> [protective relay] --trip--> [breaker]
           |--> [ammeter]
           '--> [redstone converter]
```

This keeps instrumentation off bare conductors, which per *Shock* is where it wants to be.
How a DC circuit is measured is open, instrument transformers being AC-only.

## 14. Control

**Two bridged layers.**

- A native **control circuit** layer (switches, contactors, timers) at low voltage,
  commanding the **power circuit** - real switchgear behavior, where a breaker is
  something the control circuit can trip.
- **Redstone converters** in both directions, so redstone can read and drive the
  control layer and vice versa.

## 15. Failure model

**Nothing fails because of power.** Damage is always caused by current or by voltage,
and those two fail in different shapes.

| Cause       | Physically        | Shape of failure                          | Protection                        |
|-------------|-------------------|-------------------------------------------|-----------------------------------|
| **Current** | heat              | cumulative, builds and drains             | breakers, sized in amps           |
| **Voltage** | dielectric stress | faster the further over, never cumulative | arresters, and breakers  |
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
generator coils, transformer coils, and contacts. Each holds a heat state that rises
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

**The one place over-voltage does current damage is a saturating coil.** Per
*Transformers* a coil fed over its winding rating draws magnetizing current rather than
breaking down, so it runs the thermal chain above and not this wall. Its bushings still
carry its class, and flashing one of those over is the ordinary dielectric case.

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

Three devices, each covering something the others cannot.

**A fuse is the floor.** One block, in line with the conductor it protects. It senses and
interrupts in the same body, needs no configuration and nothing bolted to it, and is
destroyed when it operates. Sized in amps. A fuse is the first protection a circuit gets
and the only one that works with no control circuit anywhere.

**A breaker opens on command and senses nothing.** It is a multiblock that interrupts a
circuit and does nothing else - per *Multiblocks* composed of working parts with no
formation step, and per *Control* something the control circuit can trip. It is resettable
where a fuse is consumed, and what commands it is open: a protective relay, a switch, a
redstone converter, an interlock.

**Protective relays are what watch the line.** A protective relay is a measuring device
that reads a tap per *Devices*, compares against a threshold, and issues a trip. It is
distinct from the contactors and switches of *Control*, which carry no measurement. There
is one relay for each damage cause:

- **Over-current relay.** Trips before heat reaches the limit, so a fault stops at the
  trip state instead of reaching burnout. Sized in amps, read from a current transformer.
- **Over-voltage relay.** Trips on line voltage above the circuit's class, read from a
  voltage transformer.

**Surge arresters clamp instead of switching.** Above its rating an arrester conducts the
excess to ground at the speed of the fault, holding voltage downstream below the withstand
level. It pays by absorbing that energy itself, and a large enough surge destroys it. An
arrester is cheap and a transformer is not, and a spent one is visible on the pole that
took the hit.

**Over-voltage splits by whether something is still pushing.**

| | Example | Cleared by |
|---|---|---|
| **Transient** | a lightning strike | the arrester alone - the surge passes, and nothing needs to open |
| **Sustained** | a generator geared into the wrong class | the arrester holding the line down while a relay trips the breaker |

A breaker is mechanical and takes a moment to open, which a gross over-voltage does not
give it; an arrester alone against a sustained fault keeps dying into a circuit that is
still live. Together they clear it with nothing expensive lost.

**Lightning strikes the grid.** A strike lands on an arrester where one is present and
destroys it. What a strike does to an unprotected line, and whether strikes are drawn to
tall poles, is open.

## 16. Shock

**A bare energized conductor is dangerous to touch.** That hazard is the whole reason
insulation exists in this mod; per *Failure model* a jacket has nothing to do with voltage
class.

Catenary wire and busbars are bare, so both are live whenever the grid is. Surface wiring
is jacketed and safe to be around. The intent is that bare conductors live where people
are not: catenary slung overhead out of reach, busbars in a plant room entered
deliberately rather than a corridor walked down.

As with arcs, severity follows the circuit - what a contact costs scales with what is
behind it.

## 17. Create integration surface

In scope:

- **Engineer's Goggles overlay** - electrical state shown in Create's own overlay
  idiom.
- **Ponder scenes** - every block taught by an animated scene.
- **Works on contraptions** - wiring and devices survive assembly onto a moving
  contraption.

Out of scope: **trains** - no electrified rail, pantographs, overhead line, or
battery locomotives.

## 18. First vertical slice - generators

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

**The scaling law follows from *Electrical quantities*.** A segment is copper in series down
the stack, so what a segment adds is the current the machine can carry, and the shaft's speed
is the voltage it produces. Neither of them is what the machine draws: that is its grid's to
decide, and the solve decides it.

| Physical fact         | Electrical consequence                           |
|-----------------------|--------------------------------------------------|
| Segments in the stack | **Rated current** - each segment adds a fixed `I` |
| Shaft speed           | **Voltage**                                      |
| The two multiplied    | **Rated watts** - what the machine can deliver   |

What the machine actually delivers is what its grid draws, and the stress Create sees is that
wattage - power is conserved across the generator. So a longer stack is a higher-current
machine and gearing the shaft faster is a higher-voltage one. The two ways to build a bigger
generator are to lengthen the stack or to gear it up, and they are not interchangeable: they
land in different places on the transmission-loss curve, and they run out differently - a
stack worked past its rated current burns copper per *Failure model*, where voltage is capped
by the class of what the machine is wired to.

### The rotor

**A magnetised shaft is assembled, not crafted.** A Create shaft runs through a sequenced
assembly that deploys **four magnets over four passes**, carrying an incomplete generator shaft
between them. The centrepiece of the chain is built on a line rather than in a grid.

**The finished shaft registers as an item.** Whether it is the block a stack is built from, or a
fitting that goes into a frame block the way coils go into a transformer per *Multiblocks*, is
open, and settles with the generator itself. The assembly above holds either way: the recipe is
the same, and a shaft that becomes a block keeps the id it carries as an item.

## 19. Registration

**The mod id lives in a shared package**, not on the `@Mod` class. Per *Package Structure* nothing
may import the mod root, so a feature that needs to name what it registers would otherwise have
nothing to name it with. `Namespace.ID` is the one place the string `septemcraft` appears in code,
and `gradle.properties` holds the other copy that `mods.toml` is expanded from.

**A feature registers its own objects and exposes them through its one public type.** The
`DeferredRegister` is built inside the feature and handed the mod event bus by the module root,
which is handed it by the mod root. Nothing is static: each is constructed with what it needs.

**One creative tab, `SeptemCraft`**, iconed with the magnet. The mod root registers it and fills it
by asking each module for its items, because per *Package Structure* whatever enumerates features is
a composition root.

**Data generation is one provider per kind, filled by the modules.** A locale is one file, so two
language providers would collide; rather than split the rule by provider, all four - language, item
models, recipes and tags - live at the mod root and each module contributes its own content into
them. A module's recipes, names and models stay the module's to write.

**Recipes are values.** A Create processing recipe is a record in a shared package, not JSON written
by hand and not Create's own recipe builders, so a recipe is type-checked at compile time and the
mod holds no compile dependency on Create's datagen internals. What a wrong key still costs is
caught by a GameTest, which loads the generated pack in a real world and checks every declared
recipe came back under the Create type it was written for.
