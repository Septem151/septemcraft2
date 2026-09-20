package io.gifsync.septemcraft.api;

import java.util.List;

/** A circuit as assembled, which is a set of nodes and the things joining them. */
public interface Circuit
{
	/** Every node in the circuit, in the order the builder minted them. */
	List<NodeId> nodes();

	/**
	 * The node every potential in the circuit is measured from, which is the first one minted. Its
	 * own potential is nothing.
	 *
	 * <p>A circuit need not hang together: a transformer's secondary shares no metal with its
	 * primary, and a run joined to nothing shares none with anything. Each such part is measured
	 * from a point of its own, and only this one is promised to be the node named here. Comparing a
	 * potential in one part against a potential in another is meaningless, however the two numbers
	 * happen to fall.
	 */
	NodeId reference();

	/** Every two-terminal element in the circuit, transformer windings among them. */
	List<Element> elements();

	/** The runs of conductor in the circuit. */
	List<Conductor> conductors();

	/** The things driving the circuit. */
	List<Source> sources();

	/** The things drawing from the circuit. */
	List<Load> loads();

	/** The coupled winding pairs in the circuit. */
	List<Transformer> transformers();
}
