# Transformer Implementation Plan

Scope: transformers only, per *Transformers* in `../00-decisions.md`, plus the thermal chain in
*Failure model* that it defers to. Generators, wiring, batteries, breakers and devices are out of
scope - not implemented, and not stubbed beyond the interfaces named below.

Sources: `../00-decisions.md` sections 12 (Transformers), 15 (Failure model) and 7 (Current types).
`01-open-questions.md` Q9, Q18, Q19 and Q20 are blocking and are not to be answered here.

## Standing rules

1. **No agent resolves an open question.** Where a task needs one, it produces the seam - an
   interface, or a named constant - and reports the blockage. Picking a plausible number quietly is
   the failure mode this plan exists to prevent.
2. **No magic numbers.** Every quantity traces to a named constant in `TransformerConstants`
   carrying a comment that cites its section or Q-number.
3. Anything encoding a design decision cites `../00-decisions.md` section N in its javadoc.
4. **The model layer is plain Java, free of Forge types.** Block entities, capabilities and
   rendering are a thin shell over it, added later. An agent that finds this untenable reports back
   rather than importing Forge into the model.
5. Package root: `io.gifsync.septemcraft.electrical.transformer`.

## Order

```
T1 ──▶ T2 ──┬──▶ T3 ──┐
            └──▶ T4 ──┴──▶ T5 ──▶ T6 ──┬──▶ T7 ──▶ T9
                                        └──▶ T8 ──┘
```

T3 and T4 run in parallel, as do T7 and T8. Everything else is sequential.

## T1. Constants and quantity substrate

Depends on nothing; blocks everything. Runs alone, first.

Create the package and `TransformerConstants`, holding each value as a named constant with a
provenance comment:

| Constant                  | Status                                                      |
|---------------------------|-------------------------------------------------------------|
| `VOLTS_PER_WINDING`       | provisional - Q18                                           |
| `MAX_WINDINGS_PER_BLOCK`  | provisional - Q18                                           |
| `COPPER_BUDGET_PER_BLOCK` | provisional - Q18                                           |
| `TRANSFORMER_LOSS_FRACTION` | provisional - section 12 fixes it as small and below a rectifier's; the number is unstated |
| `LV_CEILING`, `MV_CEILING` | provisional - Q9                                           |

Establish `Volts`, `Watts` and `Amperes` as records guarding non-negative and finite, and
`Circuit<AC>` / `Circuit<DC>` phantom typing per *Current types*. The transformer API is AC-only by
signature, not by a runtime check.

**Acceptance.** Every provisional value is reachable from one file, and the rest of the package
contains no numeric literals but 0, 1 and 2.

## T2. The coil

Depends on T1.

`Coil` carries `blockCount` and `windingCount`. Invariants: at least one winding, and
`windingCount <= blockCount * MAX_WINDINGS_PER_BLOCK`.

Derived, per *Blocks are watts*:

```
  ratedVoltage = windings x VOLTS_PER_WINDING
  ampacity     = COPPER_BUDGET_PER_BLOCK x blocks / windings
  ratedPower   = VOLTS_PER_WINDING x COPPER_BUDGET_PER_BLOCK x blocks
```

**Acceptance.** A test sweeping winding counts at fixed block count asserts `ratedPower` does not
move. That a transformer's wattage is its size and nothing else is the load-bearing claim of
section 12, and it is enforced by a test rather than by a comment.

## T3. Pairing and geometry - partially blocked, Q20

Depends on T2. Parallel with T4.

Two adjacent coils are a transformer, with no formation step. Effective height is the shorter of
the two; blocks above it are idle.

Provide `CoilPairing` as an interface with one implementation resolving the unambiguous two-coil
case only. A run of three or more coils returns unresolved and says why. No facing rule, no
bushing-derived rule, no refusal rule - Q20 lists exactly those three as the open options.

**Acceptance.** A 2x1 forms. A bare rod on either side does not. Three in a row returns unresolved
rather than picking a partner. Unequal heights yield the shorter height with the surplus reported
idle.

## T4. Bushings and voltage class - partially blocked, Q9

Depends on T2. Parallel with T3.

`VoltageClass` is an enum of LV, MV and HV; the three classes are settled and their boundaries are
not, so comparisons read T1's provisional ceilings.

`Bushing` carries a class. A circuit is two conductors, so each coil takes two bushings and a
two-coil transformer takes four. A transformer's class is that of its weakest bushing, implemented
as a min over the four. Re-classing is replacing bushings and touches no other state.

**Acceptance.** The weakest-bushing rule is tested: three HV bushings and one LV give an LV
transformer. The transformer itself carries no class field.

## T5. Ratio and transfer semantics

Depends on T3 and T4.

`transfer(Circuit<AC>) -> Circuit<AC>`, where `V_out = V_in x (w_out / w_in)`,
`P_out = P_in x (1 - TRANSFORMER_LOSS_FRACTION)`, and current stays derived as P/V.

Three rules from section 12 that are easy to get wrong:

- **Primary and secondary are symmetric.** Whichever pair of bushings is fed is the input. No
  direction is stored.
- **The winding rating is a ceiling, not a floor** - the inverse of a device rating. Fed under it
  the transformer works and output scales with the ratio. Fed over it the coil saturates, which is a
  current event handed to T6 and never a dielectric one.
- **Ratio is not a property.** No ratio field is stored or exposed. It is the two ratings against
  each other.

**Acceptance.** Feeding either side gives the mirrored result. A `32 V` to `64 V` and a `128 V` to
`256 V` transformer behave differently despite both being 1:2 - an explicit test, this being the
stated reason ratio is not a field. A matched step-up and step-down pair round-trips to the input
less exactly two loss deductions.

## T6. Thermal, saturation and oil - cross-cutting

Depends on T5.

*Failure model* sets one curve for every threshold in the mod. No transformer-local thermal curve is
written. This task defines the interface the coil presents to that shared curve - an ampacity, and a
heat state rising over ampacity and draining under it - and reports that the curve itself belongs to
a failure-model module outside this scope.

Oil is consumed only above ampacity. Over-ampacity boils it off, a dry coil heats faster, and the
escalation is overload, boil-off, dry, burnout.

The trap: loss is a deduction and not heat. A transformer at full rated load with its loss fraction
applied sits at zero heat and full oil indefinitely.

**Acceptance.** A coil held at exactly ampacity across a long simulated run ends with heat at zero
and oil untouched. A coil fed over its winding rating accumulates heat and triggers no dielectric
path.

## T7. Arrangements - partially blocked, Q19

Depends on T6. Parallel with T8.

A power transformer and a voltage transformer are the same hardware, and the wiring is the entire
difference: a power transformer's primary is in series with the circuit terminating at its bushings,
a voltage transformer's primary sits across a circuit carrying on past. **No mode field, flag or
setting exists on the block or its model type.** An agent that adds one has misread the section.

The consequence to implement: a voltage-sized coil wired in series runs the thermal chain and burns
out. That is a designed mistake with a designed punishment, and it falls out of T6 without
special-casing.

The current transformer is a separate single-coil construction - a looped core the conductor
threads, one turn as its primary, a bushing where the conductor enters and one where it leaves, and
a tap for the reading. The line is unbroken and loses nothing. The tap is Q19: the reading output
goes behind an interface and stops there.

**Acceptance.** No mode field exists. A voltage transformer in a power path heats by the ordinary
thermal path. The current transformer applies zero loss to the through-conductor.

## T8. Burnout wreckage

Depends on T6. Parallel with T7.

Burnout leaves a burnt variant in place. It is recyclable, crushing or melting returning a fraction
of its copper, and its magnets are lost.

## T9. Open-question audit

Depends on all. Run last, by a fresh agent that wrote none of the code.

Enumerate every provisional constant and every deferred interface, map each to its Q-number, and
cross-check against `01-open-questions.md`. Report any place an open question was resolved
implicitly - a chosen pairing rule, a picked class boundary, an invented tap. The output is a
report, not a code change.

## Out of scope

Generators, batteries, wiring, breakers, instrument wiring beyond the current transformer's stub,
the shared thermal curve, and all Forge registration. Q9, Q16, Q17, Q18, Q19 and Q20 stay open; four
of them bear directly on transformers, and T9 exists to confirm none was answered by accident.
