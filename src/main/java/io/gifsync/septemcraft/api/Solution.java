package io.gifsync.septemcraft.api;

/**
 * What a circuit reads once it has been solved. Every reading is answerable whatever became of the
 * attempt: a circuit that was not solved reads as zero throughout rather than refusing to be read.
 */
public interface Solution
{
	/** What became of the attempt that produced these readings. */
	SolutionStatus status();

	/**
	 * The potential at a node, against the circuit's own reference. Two nodes joined by a conductor
	 * that resists nothing are one point electrically, and both read alike.
	 */
	Volts voltageAt(NodeId node);

	/** The potential at one node measured against another. */
	Volts across(NodeId from, NodeId to);

	/**
	 * The current through an element, positive when it runs the way the element is oriented. A
	 * conductor that resists nothing and only circulates carries no determined current, and reads
	 * as none.
	 */
	Amperes through(Element element);

	/** The power a conductor burns. */
	Watts lostIn(Conductor conductor);

	/**
	 * The power a source delivers into the circuit at its terminals, which is what its own windings
	 * have not already taken. Negative when the circuit is driving the machine instead.
	 */
	Watts deliveredBy(Source source);

	/** The power a load draws. */
	Watts drawnBy(Load load);

	/** The power reaching a transformer's primary. */
	Watts arrivingAt(Transformer transformer);

	/** The power leaving a transformer's secondary. */
	Watts leaving(Transformer transformer);
}
