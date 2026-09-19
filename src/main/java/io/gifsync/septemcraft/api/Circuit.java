package io.gifsync.septemcraft.api;

import java.util.List;

/** A circuit as assembled, which is a set of nodes and the things joining them. */
public interface Circuit
{
	/** Every node in the circuit, in the order the builder minted them. */
	List<NodeId> nodes();

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
