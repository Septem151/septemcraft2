package io.gifsync.septemcraft.api;

/**
 * Something joining two nodes that a current can run through. Positive current runs through an
 * element from {@link #from()} to {@link #to()}, whatever the element is, so that the currents at
 * a node can be summed without asking what each of them belongs to.
 */
public interface Element
{
	/** The node a positive current through this element runs from. */
	NodeId from();

	/** The node a positive current through this element runs to. */
	NodeId to();
}
