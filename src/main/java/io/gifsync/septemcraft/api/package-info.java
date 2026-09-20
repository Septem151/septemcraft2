/**
 * The electrical contract. Declares the quantities electricity is measured in, the elements a
 * circuit is built from, and what solving one answers. Plain Java that knows nothing of a world, a
 * block or a shaft, so that anything in the mod may depend on it. This package depends on nothing.
 *
 * <p>Nothing here is implemented. Every method that would compute something throws
 * {@link java.lang.UnsupportedOperationException}, and the tests beside this package say what each
 * of them must answer once it does.
 */
@PackageKind(Kind.SHARED)
package io.gifsync.septemcraft.api;

import io.gifsync.septemcraft.structure.Kind;
import io.gifsync.septemcraft.structure.PackageKind;
