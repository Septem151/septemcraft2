# Code Principles

## Must

1. **Must inject via constructor.** A constructor receives and checks everything the object
   needs. Nothing is set afterwards - if it exists, it is ready to use.
2. **Must use enums for alternatives.** A finite set of options is an enum, never a string, an
   int, or a flag.
3. **Must type every value.** Every value a method takes or returns is a declared type, never a
   primitive or a `String`. Primitives appear only inside a type's own implementation.
4. **Must javadoc public members.** Every public type and every public method carries one
   sentence naming what it is responsible for.
5. **Must keep data immutable.** A type holding data is a Lombok `@Value` class. It may have
   methods that compute from its own fields; it cannot change after construction.

## No

6. **No public by default.** A member is `private` until a class in the same package references
   it, then package-private until a class outside the package references it.
7. **No peer imports.** Two packages at the same level never import each other. What passes
   between them is a type in a package both import.
8. **No method returns null.** `null` is never returned, passed, or stored. A value that may be
   absent is an `Optional`; an absent collection is an empty collection.
9. **No silent failure.** No empty catch, and no catch that returns a default in place of the
   failure. An error is handled where it can be, or thrown on.
