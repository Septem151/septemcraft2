/**
 * The mod's entrypoint, configuration and data generation. Wires up the modules beneath it and is
 * imported by none of them.
 */
@PackageKind(Kind.COMPOSITION_ROOT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
package io.gifsync.septemcraft;

import io.gifsync.septemcraft.structure.Kind;
import io.gifsync.septemcraft.structure.PackageKind;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
