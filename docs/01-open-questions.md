# Open Questions

Status: **ideation**.
Last updated: 2026-09-17

Companion to `00-decisions.md`. Everything here is undecided.

---

## A. Parked - raised and explicitly deferred

These were asked and set aside as premature. They are recorded so they are not
lost, not so they are answered now.

### Q1. The Create ↔ electrical quantity mapping

*Blocking: the simulation core, all tooltips, all balance.*

The stated intent is to **proportionally map Create's quantities onto electrical
ones**, so that Create power, electrical power and FE are one convertible currency.
Candidates:

- **(a) RPM → Voltage, SU → Current.** Physically correct: a real dynamo's EMF
  rises with shaft speed and its torque rises with current draw. Power is conserved
  across the conversion.
- **(b) RPM → Voltage, SU → Watts,** current derived as P/V. Closest to how Create's
  goggles already present stress as a capacity number, and to how real generators
  are actually rated.
- **(c) Loose thematic renaming.** No fixed proportionality; electrical words over
  independently-tuned numbers. Maximum balance freedom, abandons the intuition transfer.

There is also an unresolved sub-question: what "fuel cost" was meant to map to.
This could be burnable resources like coal, fluids, or something else entirely.

### Q2. Does distance cause loss?

*Blocking: Transformers and voltage tiers have a mechanical job.* Needs to be fleshed out.

### Q3. Failure model - what abuse actually does

*Blocking: breakers, fuses, insulation ratings.*

Deferred. Ranges from graceful degradation and tripping, through burnout into
recyclable items, to arc flash and explosions, possibly graded by severity.

---

## B. Asked, answered "no preference" - need a decision eventually

1. What crushed sky stone becomes, and how a magnet is made 
2. Depth of the copper winding chain recipes
3. How a generator is physically built
