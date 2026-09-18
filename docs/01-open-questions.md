# Open Questions

Status: **ideation**.
Last updated: 2026-09-17

Companion to `00-decisions.md`. Everything here is undecided. Once a question is
answered its entry is **deleted outright** - the answer lives in `00-decisions.md`
and nothing is left behind here to point at it. This is a living, constantly changing document.

Numbers are never reused. A deleted question's number stays retired, so the gaps in the
sequence are expected and a number can only ever mean the one question it was given to.

---

## Q9. Where the voltage class boundaries sit

*Blocking: insulation recipes, device ratings, transformer ratios, generator gearing.*

*Failure model* settles that there are three voltage classes - LV, MV and HV - and that
a component's class ceiling is its withstand voltage. The voltages those ceilings take
are open, as is which class the first generators and their loads sit in.

## Q10. Which current type motors and lighting speak

*Blocking: the motor and lamp blocks.*

*Current types* settles the DC domain as battery internals, control circuits, and
sensors and instrumentation, and settles that generation is AC. *Devices* puts motors
and lighting on the grid alongside the rest. Whether a motor and a lamp take AC off the
line directly, or sit behind a rectifier like the other local loads, is open.

## Q12. The dandelion rubber chain

*Blocking: insulated wire, and so surface wiring.*

*Materials* settles the jacket as rubber, made by crushing dandelions for latex and
vulcanizing it into sheet. What sits between the flower and the sheet is open: how latex
is coagulated, what stands in for sulfur as the curing agent, and how many stages the
chain runs to.

Yield sits behind it. Interior wiring is the domestic layer and wants to be buildable
without a dedicated industry, so how much latex a flower gives decides whether the jacket
costs a flowerbed or a field.

## Q13. What lightning does to an unprotected line

*Blocking: how exposed an outdoor line is, and how many arresters it wants.*

*Protection* settles that lightning strikes the grid and destroys an arrester where one is
present. What a strike does where none is present is open, as is whether strikes are drawn
to tall poles - which would make catenary height a liability and give arrester placement a
geometry. Whether spent arresters turn storms into a maintenance loop sits behind both.

## Q14. Where oil comes from

*Blocking: refilling a transformer after an overload.*

*Materials* settles oil as the transformer coolant and *Transformers* settles that nothing
but overload consumes it. Its chain is open: Create has no oil, so oil needs a source the
way rubber got dandelions. Yield decides what recovering from an overload costs, and so how
harsh the boil-off, dry, burnout escalation is in practice.

Water stands in for oil until the chain is decided.

## Q15. One protective relay or two

*Blocking: the relay block.*

*Protection* settles an over-current relay and an over-voltage relay as separate items. A
protective relay is a threshold comparator, so what it watches could be a setting on one
block rather than two devices. Whether the pair collapses is open.

## Q16. How a DC circuit is measured

*Blocking: instrumentation and protection on the DC domain.*

*Devices* settles the instrument transformer as the measurement primitive, and
*Transformers* settles that it is AC-only. Per *Current types* the DC domain is battery
internals, control circuits and instrumentation, and per *Arcs* a shorted battery explodes,
so DC is not a place to go unmeasured. Whether DC gets its own tap, or instruments read a
DC circuit by contact because low voltage makes that safe, is open.

## Q17. What a breaker is made of

*Blocking: the breaker multiblock.*

*Protection* settles the breaker as a multiblock that interrupts on command. Its parts are
open. Contacts carry current and so want an ampacity and a thermal state per *Failure
model*; whether rating follows part count, as it does for generators and transformers, is
open with them.

## Q18. Volts per winding, and how many windings a block holds

*Blocking: transformer sizes, and so the footprint of every substation.*

*Transformers* settles that a coil's voltage rating is its winding count times a fixed
volts-per-winding, and that a block holds windings up to a cap. Neither number is set, and
between them they decide how many blocks a voltage costs: at 32 V a winding and 8 windings
to a block a 1,024 V coil is four blocks tall, and at 64 V a winding it is two. The copper
budget a block contributes - the third constant, the one that turns block count into watts
- is open alongside them.

All three sit behind Q9, which sets the voltages the classes take.

## Q19. What a tap is

*Blocking: voltage transformers, current transformers, and the control wiring of every
instrument.*

*Transformers* settles the bushing as the block a circuit's conductor lands on, and settles
the tap as something else: what a current transformer's reading leaves by, and what a
voltage transformer's primary takes off a circuit carrying on past. What a tap physically
is, and whether it carries a pair of conductors as a circuit does or a single control
cable, is open. Whether a busbar and a catenary span are branched the same way sits with
it.

## Q20. What decides which two coils pair

*Blocking: transformer placement, and so the layout of a plant room.*

*Transformers* settles a transformer as two adjacent coils with no formation step. A row of
three or more transformer blocks is therefore ambiguous - which neighbour a coil couples to
is undecided, as is whether a coil already paired is still available to the one on its far
side. Whether the blocks carry a facing that resolves it, whether bushing placement does,
or whether a run longer than two is simply refused, is open.
