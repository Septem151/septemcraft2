package io.gifsync.septemcraft.api;

import java.util.List;

/** A circuit as the builder assembled it, held against change once it is handed out. */
record AssembledCircuit(List<NodeId> nodes, NodeId reference, List<Element> elements, List<Conductor> conductors,
	List<Source> sources, List<Load> loads, List<Transformer> transformers) implements Circuit
{
	/** Holds every list against change, so a circuit handed out cannot be edited behind the builder. */
	AssembledCircuit
	{
		nodes = List.copyOf(nodes);
		elements = List.copyOf(elements);
		conductors = List.copyOf(conductors);
		sources = List.copyOf(sources);
		loads = List.copyOf(loads);
		transformers = List.copyOf(transformers);
	}
}
