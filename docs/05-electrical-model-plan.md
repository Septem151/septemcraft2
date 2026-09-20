# Electrical Model Implementation Plan

Scope: the quantity mapping, the circuit solver, and the seam to Create's kinetic network. No
blocks, no items, no registration, no rendering, no datagen. The generator's construction - what a
stator is, how it is wound, how many housings make a machine - is undecided and deliberately not
needed here. A new agent can finish this plan without it.

Sources: *Electrical quantities*, *Transmission range* and *Failure model* in `../00-decisions.md`,
which records the model this plan is written against.

**Built: E1 to E5**, in `io.gifsync.septemcraft.api`, with the fixtures below as the JUnit corpus in
`src/test/java/io/gifsync/septemcraft/api`. E6 to E9 are not built.

## The model

Rotation and electricity are one two-quantity system. Mechanically power is torque times angular
velocity; electrically it is voltage times current. A real machine trades one pair for the other,
and the mapping follows from that rather than from preference:

| Electrical     | In Create                                | Relation                             |
|----------------|------------------------------------------|--------------------------------------|
| **Voltage**    | shaft RPM at the machine                 | `V = VOLTS_PER_RPM x RPM`            |
| **Current**    | torque, which Create calls stress impact | `I = su/RPM`                         |
| **Power**      | stress units                             | `P = V x I`, and `SU = impact x RPM` |
| **Resistance** | RPM squared per stress unit              | `R = V / I`                          |

At one volt per RPM and one watt per stress unit, **one ampere is one su/RPM exactly**, and one ohm
is one RPM per su/RPM. Watts and total stress stay one currency, so power is conserved across every
machine and nothing is created at the boundary.

Two consequences that are easy to miss:

- **A machine with a fixed stress impact is a constant-current device**, not a resistor. A true
  resistor's impact rises with the speed it sees, because it draws `V/R` at `V` volts and so eats
  `V squared / R` of stress. Both are expressible; the solver must not assume the first.
- **A generator's impact is the current its grid is drawing.** Unloaded it costs almost nothing to
  turn; loaded it costs exactly its wattage. Nothing about the machine's construction sets its
  current - its copper sets the current it can *survive*.

## The solutions

**Full nodal analysis over the whole circuit.** The alternative considered and rejected was a
radial sweep with declared load classes, which is cheaper and simpler but cannot represent a ring
busbar or two generators on one bus. Both of those are builds this mod expects people to make, and
the second produces reverse power flow - a fast machine motoring a slow one through the grid, and
in Create terms spinning that machine's water wheels. This is the kind of consequence the mod
exists to have.

What that buys, and what it costs, are recorded here so it is not relitigated: the arithmetic is
identical to a radial sweep on every single-feed build. Nodal analysis is not more accurate. It is
more *permissive*, and its cost is a linear solve that has to be numerically right.

## Standing rules

1. **No agent resolves an open question.** Where a task needs one, it produces the seam - an
   interface, or a named constant - and reports the blockage.
2. **No magic numbers.** Every quantity traces to a named constant carrying its provenance. The
   constants file is the only place provenance is written; Javadoc describes the member it is on and
   cites nothing.
3. **The circuit package is plain Java.** No Minecraft type, no Forge type, no Create type. It is
   tested by JUnit, which needs no world. An agent that finds this untenable reports back rather
   than importing.
4. **Quantities may be negative.** Reverse power is a first-class outcome, not an error: a machine
   can be driven by its own grid. A quantity record guards finiteness and nothing else. An agent
   that adds a non-negative check has misread this.
5. Package roots: `io.gifsync.septemcraft.api` - shared, plain Java, the quantities, the elements a
   circuit is built from, the solver, and what solving one answers.
   `io.gifsync.septemcraft.electrification.grid` - shared, the live level-wide network and its
   persistence. Neither may import a feature.

## Order

```
E1 ──▶ E2 ──▶ E3 ──▶ E4 ──▶ E5 ──┬──▶ E6 ──┐
                                  ├──▶ E7 ──┼──▶ E9
                                  └──▶ E8 ──┘
```

E6, E7 and E8 run in parallel. Everything else is sequential.

## E1. Quantities and constants

Depends on nothing; blocks everything.

`Volts`, `Amperes`, `Watts` and `Ohms` as records over a finite double, signed. Arithmetic that
crosses them returns the right type - volts over ohms is amperes - so a wrong pairing does not
compile.

`ElectricalConstants` holds them, and holds their provenance:

| Constant                       | Status                                                                  |
|--------------------------------|-------------------------------------------------------------------------|
| `VOLTS_PER_RPM`                | settled at 1                                                            |
| `CATENARY_WIRE_OHMS_PER_BLOCK` | provisional at 0.01 - no wire gauge exists, so a form has one figure    |
| `BUSBAR_OHMS_PER_BLOCK`        | provisional at 0.002 - a fifth of wire, being the spine of a plant room |
| `SINGULARITY_THRESHOLD`        | provisional at 1e-12 - every conductance stamped is orders above it     |
| `CONVERGENCE_TOLERANCE`        | provisional at 1e-9                                                     |
| `RELAXATION_FACTOR`            | provisional at 0.75 - below one it damps                                |
| `MAX_ITERATIONS`               | provisional at 100                                                      |
| `MAX_NODES_PER_CIRCUIT`        | provisional at 256 - the ceiling the dense solver of E3 is chosen under |

Two constants named here are not in that file. `WATTS_PER_STRESS_UNIT` is not, because nothing in the
package converts stress, and it belongs with the seam of E6 that does. `WINDING_RESISTANCE` is not,
because a machine is handed what its own windings resist, so the figure belongs to whatever builds
one. *Wiring* in `../00-decisions.md` names three conductor forms; the two the fixtures pin carry a
figure and surface wiring does not yet.

**Acceptance.** Every provisional value is reachable from one file, and the rest of both packages
holds no numeric literal but 0, 1 and 2.

## E2. The circuit as a value

Depends on E1.

An immutable description of one electrical circuit: nodes, resistive edges, voltage sources carrying
an electromotive force and an internal resistance, and loads attached to nodes. The live network of
E7 builds a new one when topology changes; nothing mutates a circuit in place.

**Every circuit is two conductors.** Both are modeled - a line out and a line back - rather than
one line with an implied return. It doubles the node count, and it is what makes a short a
consequence rather than a special case.

**Zero-resistance edges are contracted before solving.** Two blocks bolted together are one node.
Done with a union-find pass at construction, which also removes the singular matrix a 0 ohm edge
would otherwise hand E3.

**Every element carries an identity the builder mints.** Two runs of the same conductor between the
same pair of nodes are a thing a built grid really holds. Each has a reading of its own, so an
element is not identified by what it is made of and where it lands.

**The first node the builder mints is the circuit's reference.** A voltage is measured from the
reference of whichever galvanically joined part its node belongs to, and only the part holding that
first node is promised to be measured from it. A transformer's secondary shares no metal with its
primary, so a voltage in one compared against a voltage in the other means nothing however the two
numbers fall.

**Acceptance.** Contraction merges a chain of zero-resistance edges to a single node. A circuit
carrying no source is legal and solves to zero throughout. A circuit whose graph is disconnected
solves each part independently and neither part sees the other's sources. Two identical conductors
between one pair of nodes read separately.

## E3. The linear solve

Depends on E2.

Modified nodal analysis. One node is the voltage reference; every other node's voltage and every
source's current are unknowns.

Dense Gaussian elimination with partial pivoting is adequate at the node counts `MAX_NODES_PER_CIRCUIT`
permits, and that constant exists to keep it true. A sparse solver is a later optimization and is
not to be anticipated with an interface - the class is named directly, and a second implementation
is what earns an abstraction.

Degenerate cases are outcomes, never exceptions escaping the package: a circuit with no running
source de-energizes; a singular system is reported and de-energizes rather than propagating NaN.

**A short is reported apart from a singularity.** A source with no internal resistance carrying a
run of conductor from one of its own terminals back to the other is asked to hold a voltage across
a path that permits none. That scenario is the fault a player has built and can go and find. Anything
else the system does not determine - two ideal sources in parallel disagreeing among them - is a
singularity. A source whose windings resist anything at all is neither, however hard it is shorted:
the windings hold the current to a large figure and a real one.

**The node ceiling belongs to the solver, which is built for a number of them.** A larger circuit is
reported too large rather than solved slowly. Zero-resistance edges are contracted before the count
is taken, so a run of bolted blocks far longer than the ceiling still solves.

**Acceptance.** A resistive divider, a parallel pair and a bridge each match hand-computed values.
The ring fixture below feeds the tap opposite through one side's worth of resistance, and through
two sides' worth with one side of the ring cut. The two-generator fixture reproduces its negative current - a test that
asserts a
generator is being motored, since that is the behavior this whole approach was chosen for.

## E4. Load classes and convergence

Depends on E3.

Three load classes as an enum: constant resistance, constant current, constant power. A constant
power load is nonlinear, so it is linearized at its present terminal voltage and the solve repeats
until voltages settle within `CONVERGENCE_TOLERANCE`.

**A device below its minimum draws nothing.** That is a step down to none and not a taper towards
it, and it is what bounds a load holding its power: the harder such a device would pull as its
supply sagged, the sooner it stops pulling at all.

**The minimum is what removes the second operating point.** A constant-power load on a resistive
line has two mathematically valid answers - the fixture below settles at 96 V and 10.67 A, and also
satisfies itself at 32 V and 32 A, burning three times as much in the line as it delivers. The two
always sit either side of half the source's electromotive force, so a device whose minimum is at or
above that half is not running at the lower of them and cannot hold a circuit there. A device rated
sensibly for the bus it is on leaves the solve one answer to find, and leaving that headroom for the
line is the lesson the transmission fixtures teach.

**Where a device's minimum sits below the lower answer the circuit really does have two**, and the
solve returns the upper. Start flat, at source electromotive force, and damp.

On reaching `MAX_ITERATIONS` without settling, the last readings taken are held and the circuit
reports itself unsettled - they balance, so what is missing is only that they are final. A line
resisting more than the device on the end of it leaves room for has no answer to settle on at all,
and that is the ordinary way to reach this. An oscillating grid is not allowed to become an oscillating world. A device
whose minimum
sits above the voltage its own draw leaves it will switch off, recover, and switch off again; that
is a device sized badly for its line rather than a solver fault.

**Acceptance.** Both single-feed fixtures below, to the figures given. The single-feed load seeded
anywhere from dead to twice its source reaches 96 V every time.

## E5. Transformers as circuit elements

Depends on E4.

A two-winding ideal transformer is an element, not a voltage adjustment applied afterwards: it
couples two circuits with `V2 = n x V1` and `I1 = -n x I2`, which in nodal analysis is a current
unknown and two constraint rows.

**The element takes a ratio and asks no questions about where it came from.** How a coil's winding
count becomes a voltage rating is an open question about transformer construction, and answering it
here would be answering it for the whole mod. A loss fraction applies to power crossing the element.

**Acceptance.** The stepped fixture below holds: the same load behind a matched step-up and
step-down pair draws 2.02 A on the line rather than 10.67 A, and the line loses 12.3 W rather than
341 W. Power in equals power out less exactly one loss deduction.

## E6. The Create seam

Depends on E5. Parallel with E7 and E8.

A generator's electromotive force is its shaft speed times `VOLTS_PER_RPM`, so Create's 256 RPM
ceiling is the voltage ceiling at any machine and every volt above it comes from a transformer.

Its stress impact is the current the last solve gave it, pushed into Create with
`KineticNetwork.updateStressFor`, which takes a changed impact on a live network.

**Establish before anything depends on it: whether one block entity may cross from consuming stress
to providing it.** A motored generator draws negative current, which is a machine adding capacity to
its kinetic network rather than drawing from it. If Create will not carry that on one block entity,
the seam is two block entities or a reported blockage - never a silent clamp to zero, which would
delete the phenomenon this approach was chosen to produce.

**Create's overstress is a hard stop.** When impact exceeds capacity the whole kinetic network
halts; it does not sag. The electrical side must survive its source dropping to zero RPM and back
without oscillating between the two states.

**Acceptance.** A GameTest reads a generator's draw off Create's own figure and matches it to the
solved watts. Removing the load drops the draw to the machine's idle figure. Stalling the kinetic
network de-energises the grid and restarting it restores the same solution.

## E7. The live network

Depends on E5. Parallel with E6 and E8.

The graph is level-wide and **held in saved data, never rebuilt by scanning blocks**: a line must
keep working with the middle of it unloaded, so the middle cannot be a thing that has to tick to
exist. Blocks declare their connections as they are placed and broken, and the graph is restored
from disk rather than rediscovered.

Solving is dirty-flagged and runs at most once per tick. The triggers are enumerated in one place:
topology change, switch state, a generator's speed moving past a threshold, and a load changing
state.

**Acceptance.** A GameTest builds a line, unloads the chunks in the middle, and the far end holds
its voltage. The graph round-trips a server restart. Two changes in one tick provoke one solve.

## E8. What an instrument reads

Depends on E5. Parallel with E6 and E7.

One read API over the last solved state - a voltage at a node, a current in a branch. Meters,
current transformers, relays and breakers all read it, and **nothing computes a number of its own**.
The point of choosing a solver was that the instrumentation layer reports measurements rather than
assertions, and an instrument that re-derives its reading throws that away.

**Acceptance.** Two instruments on one branch return an identical value, and that value is the
solver's own.

## E9. Audit

Depends on all. Run last, by a fresh agent that wrote none of the code.

Enumerate every provisional constant and every deferred seam. Confirm the circuit package imports
no Minecraft, Forge or Create type. Confirm no open question was answered implicitly - a picked
winding rule, an invented conductor rating, a class boundary. The output is a report, not a change.

## Fixtures

The JUnit corpus. Figures are the converged answers, not first-pass estimates.

**Single feed.** 128 V source, 300 blocks of line at 0.01 ohms per block, a 1,024 W constant-power
load at the far end that gives up below 64 V. Solving `(128 - 3I) x I = 1024`:

| Quantity        | Value                          |
|-----------------|--------------------------------|
| Line current    | 10.67 A                        |
| Far-end voltage | 96 V                           |
| Line loss       | 341 W                          |
| Source power    | 1,365 W, and so 1,365 su drawn |

**The same feed, stepped up.** The same load behind a matched pair of transformers at 512 V:
2.02 A, 506 V at the far end, 12.3 W lost. Twenty-eight times less copper burned on one delivery.

**Two generators.** Sources of 128 V and 120 V, each with 0.5 ohms of winding resistance, on one
busbar feeding 1,024 W. The bus settles at 121.90 V; the fast machine supplies 12.20 A and the slow
machine **-3.80 A**, absorbing 463 W into its shaft. This fixture is the reason for nodal analysis
and is not to be relaxed.

**The ring busbar.** A 24-block loop of busbar - four sides of six blocks - with the load tapped
opposite the feed: two paths of two sides in parallel, which is one side, 0.012 ohms. With one side
of the ring cut, two sides in series, 0.024 ohms, and the load still fed.

## What this superseded

`../00-decisions.md` is in line with this. *Electrical quantities* has current solved for rather than
declared and a fixed stress impact as a constant-current device; *First vertical slice* has a segment
adding rated current, with what a machine draws left to its grid and the scaling law reading as a
consequence of the solve; and *Failure model* carries what a solve reports.

## Out of scope

Blocks, items, registration, rendering and datagen. The generator's construction and the stator's
geometry. Voltage classes and their boundaries. Batteries and the Forge Energy boundary. Wire
gauges, which do not exist. Reactance and power factor - **the solver is purely resistive, AC and DC
are a label on a circuit and nothing in the mathematics tells them apart**, and no task above may
assume otherwise.
