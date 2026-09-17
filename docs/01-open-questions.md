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

## Q13. Whether weather can strike the grid

*Blocking: what surge arresters are for, and how many a line needs.*

*Failure model* settles surge arresters as the protection against an over-voltage too
fast for a breaker, but nothing in the mod currently produces one. A thunderstorm striking
a catenary span is the obvious source, and the real reason arresters exist at all. Whether
the grid is exposed to weather - and if so, whether a strike is drawn to tall poles, what
it does to an unprotected line, and whether arresters are consumable enough to make
storms a maintenance loop - is open.
