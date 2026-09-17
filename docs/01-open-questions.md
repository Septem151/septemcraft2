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

*Electrical quantities* settles that over-voltage has consequences; what they are is
open. Over-current from an overloaded line and over-voltage at a device may not
deserve the same answer.

The other side is narrower: under-voltage means a device does not run, period. Components
do not degrade gracefully, they cut out.

## Q2. What "fuel cost" maps to

Burnable resources like coal, fluids, or something else entirely.

*Energy flow* puts everything upstream of the generator in Create's hands, which
would place fuel in Create's boiler rather than anywhere in this mod - leaving this
mod with no fuel concept at all.

## Q3. Which current type each device speaks

*Blocking: the generator terminal, and so the first vertical slice.*

*Current types* settles what AC and DC are *for* - AC wherever voltage must change,
DC for the local domain - and *First vertical slice* settles the generator's shape,
but not what comes out of its terminal. A rotating machine producing AC is the
real-world answer and would mean every DC circuit begins at a rectifier or battery.

## Q5. Whether AC/DC conversion costs anything

*Current types* puts rectifiers and inverters at the boundaries between the two
domains. Whether conversion is lossy, and whether that loss is large enough to be a
design pressure or is only flavor, is open. It interacts with Q3: if generators are
AC-native, a lossy rectifier taxes the whole DC domain.

## Q6. What crushed sky stone becomes, and how a magnet is made

*Blocking: the alternator segment recipe, and so the first vertical slice.*

*Materials* settles sky stone as the magnetic material and Create's crushing wheels
as the processing verb. What the crushed output is, and what sits between it and a
finished magnet, is open. Each alternator segment in the axial stack carries one.

## Q7. Depth of the copper winding chain

*Blocking: alternator segment and wire recipes.*

*Materials* settles copper as the conductor, worked through Create into wire. How
many steps separate an ingot from a finished winding is open, as is how windings
become the coils inside an alternator segment.

Copper has two consumers with very different appetites - a handful of windings per
segment, against bulk catenary wire for long-haul transmission - so the chain may
not want to be one depth throughout.
