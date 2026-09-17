# Open Questions

Status: **ideation**.
Last updated: 2026-09-17

Companion to `00-decisions.md`. Everything here is undecided. Once a question is
answered its entry is **deleted outright** - the answer lives in `00-decisions.md`
and nothing is left behind here to point at it. This is a living, constantly changing document.

---

## Q1. Failure model - what abuse actually does

*Blocking: breakers, fuses, insulation ratings.*

Ranges from graceful degradation and tripping, through burnout into recyclable
items, to arc flash and explosions, possibly graded by severity.

## Q2. What "fuel cost" maps to

Burnable resources like coal, fluids, or something else entirely.

*Energy flow* puts everything upstream of the generator in Create's hands, which
would place fuel in Create's boiler rather than anywhere in this mod.

## Q3. Does AC have a required role, and what is DC for?

*Blocking: the high-voltage transmission story, and whether* Current types *describes a real choice.*

*Current types* has both AC and DC from the start, with the player choosing per
circuit. Transmission loss gives *high voltage* a job; nothing gives *AC* one, as
the loss behavior is indifferent to current type.

One candidate is the real constraint that transformers work only on AC, which
decides it outright. Otherwise, DC needs a distinct advantage of its own, or the
per-circuit choice collapses into "always AC".

## Q4. What crushed sky stone becomes, and how a magnet is made

*Blocking: the generator recipe chain, and so the first vertical slice.*

*Materials* settles sky stone as the magnetic material and Create's crushing wheels
as the processing verb. What the crushed output is, and what sits between it and a
finished magnet, is open.

## Q5. Depth of the copper winding chain

*Blocking: generator and wire recipes.*

*Materials* settles copper as the conductor, worked through Create into wire. How
many steps separate an ingot from a finished winding - a single craft, or a staged
drawing chain - is open, as well as how finished windings will be used to make coils
when a finished generator is built.

## Q6. How a generator is physically built

*Blocking: the first vertical slice.*

*Multiblocks* rules out a formation step, so a generator is some arrangement of
parts that works by adjacency and correct wiring. Which parts, and how they sit
together, is open.
