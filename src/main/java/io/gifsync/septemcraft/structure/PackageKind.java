package io.gifsync.septemcraft.structure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares what kind of package this is. Every package carries one. */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.CLASS)
public @interface PackageKind
{
	/** The kind this package is. */
	Kind value();
}