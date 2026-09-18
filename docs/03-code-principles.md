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
5. **Must keep data immutable.** A type holding data is a `record`, and its compact constructor
   does the checking. It may have methods that compute from its own components; it cannot change
   after construction.
6. **Must keep changing state in one value.** An object the game keeps alive holds its changing
   state as one immutable value and replaces it wholesale. It declares no other mutable field.
   Fields a dependency's class declares are that class's to manage.

## No

7. **No public by default.** A member is `private` until a class in the same package references
   it, then package-private until a class outside the package references it.
8. **No method returns null.** `null` is never returned, passed, or stored. A value that may be
   absent is an `Optional`; an absent collection is an empty collection.
9. **No silent failure.** No empty catch, and no catch that returns a default in place of the
   failure. An error is handled where it can be, or thrown on.
10. **No type is declared inside another.** One file, one class - every type has a name and a file
    of its own. An anonymous class handed to a callback is an expression, not a declaration.
11. **No class assumes its side.** A type that exists only on a client is named only by a class a
    client entry point reaches; nothing a server loads may name one. World state changes only where
    `level.isClientSide` is false.
12. **No name that has to be searched for.** A type is named for the feature it belongs to and
    the role it fills - `GeneratorBlock`, `GeneratorBlockEntity`, `GeneratorRenderer`. A test
    carries the name of what it tests and ends in `Test`. Where the role has a conventional suffix,
    the name ends in that suffix.
13. **No interface with one implementation.** The class is named directly. An interface, an
    abstract class or a factory appears when a second implementation exists, never in anticipation
    of one.

## The boundary

A method the game or a dependency calls - an override, an event handler, a registration
lambda, a constructor the loader invokes - takes and returns exactly what that signature
declares. It converts on its first statement and holds no behaviour of its own. A value the
caller may hand back as `null` becomes an `Optional` on that same line, and an `Optional`
becomes `null` again only in a `return`. Its only branch is on what the signature hands it.
Any other line that breaks a rule above breaks the boundary.
