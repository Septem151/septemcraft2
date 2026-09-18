package io.gifsync.septemcraft.structure;

/** What a package is, and so what it is allowed to import. */
public enum Kind
{
	/**
	 * Exists to be depended on. Imports neither a feature nor a composition root, and anything
	 * may import it.
	 */
	SHARED,

	/**
	 * A self-contained unit. Imports shared packages, never another feature, and is reached from
	 * outside only by the composition root nearest enclosing it, through the one public type its
	 * root package exposes.
	 */
	FEATURE,

	/**
	 * Assembles features. The only kind permitted to import a feature, imports the roots it
	 * contains, and is itself imported only by the composition root nearest enclosing it.
	 */
	COMPOSITION_ROOT
}
