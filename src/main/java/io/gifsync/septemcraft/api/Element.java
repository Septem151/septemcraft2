package io.gifsync.septemcraft.api;

/**
 * Something joining two nodes that a current can run through. Positive current runs through an
 * element from {@link #from()} to {@link #to()}, whatever the element is, so that the currents at
 * a node can be summed without asking what each of them belongs to.
 *
 * <p>A source is the one element whose orientation is not a potential drop: it raises {@link #to()}
 * above {@link #from()}, where every other element's {@link #from()} is the higher of the two when
 * its current is positive.
 */
public interface Element
{
	/** Which element in its circuit this is, which is what tells two otherwise alike elements apart. */
	ElementId id();

	/** The node a positive current through this element runs from. */
	NodeId from();

	/** The node a positive current through this element runs to. */
	NodeId to();
}
