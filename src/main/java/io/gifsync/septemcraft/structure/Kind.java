package io.gifsync.septemcraft.structure;

/** What a package is, and so what it is allowed to import. */
public enum Kind
{
	/** Exists to be depended on. Imports other shared packages and nothing else. */
	SHARED,

	/** A self-contained unit. Imports shared packages, and never another feature. */
	FEATURE,

	/** Assembles features. The only kind permitted to import a feature, and imported by nothing. */
	COMPOSITION_ROOT
}