package io.gifsync.septemcraft.electrification.circuit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One electrical circuit, whole and unchanging: its nodes, the conductors between them, the machines
 * driving it, the devices drawing from it, and the transformers coupling parts of it. A circuit is
 * never edited - a topology change builds a new one.
 */
record Circuit(List<NodeId> nodes, List<Conductor> conductors, List<Source> sources, List<Load> loads,
	List<Transformer> transformers)
{
	/** Checks that the nodes are distinct, the elements are distinct, and every element joins nodes it has. */
	Circuit
	{
		nodes = List.copyOf(nodes);
		conductors = List.copyOf(conductors);
		sources = List.copyOf(sources);
		loads = List.copyOf(loads);
		transformers = List.copyOf(transformers);

		Set<NodeId> known = new HashSet<>(nodes);
		if (known.size() != nodes.size())
		{
			throw new IllegalArgumentException("A circuit names each of its nodes once");
		}

		requireDistinct(conductors, "conductor");
		requireDistinct(sources, "source");
		requireDistinct(loads, "load");
		requireDistinct(transformers, "transformer");

		for (Conductor conductor : conductors)
		{
			requireKnown(known, conductor.from(), conductor.to());
		}

		for (Source source : sources)
		{
			requireKnown(known, source.positive(), source.negative());
		}

		for (Load load : loads)
		{
			requireKnown(known, load.from(), load.to());
		}

		for (Transformer transformer : transformers)
		{
			requireKnown(known, transformer.primaryPositive(), transformer.primaryNegative());
			requireKnown(known, transformer.secondaryPositive(), transformer.secondaryNegative());
		}
	}

	/** The nodes this circuit's resistanceless conductors merge into one another. */
	NodeContraction contraction()
	{
		return NodeContraction.of(nodes, conductors);
	}

	/** Whether any machine on this circuit is turning, which nothing is on a circuit with no source. */
	boolean isEnergised()
	{
		return sources.stream().anyMatch(Source::isRunning);
	}

	/** Whether any device on this circuit answers to its voltage, which is what forces an iteration. */
	boolean isNonlinear()
	{
		return loads.stream().anyMatch(load -> load.loadClass() == LoadClass.CONSTANT_POWER);
	}

	/**
	 * The nodes this circuit joins galvanically, one group per part the current cannot cross between. A
	 * winding is copper, so it joins its own two terminals; a core is not, so it joins nothing.
	 */
	List<List<NodeId>> galvanicGroups()
	{
		List<Conductor> joins = new ArrayList<>();
		for (Conductor conductor : conductors)
		{
			joins.add(new Conductor(conductor.from(), conductor.to(), Ohms.ZERO));
		}

		for (Source source : sources)
		{
			joins.add(new Conductor(source.positive(), source.negative(), Ohms.ZERO));
		}

		for (Load load : loads)
		{
			joins.add(new Conductor(load.from(), load.to(), Ohms.ZERO));
		}

		for (Transformer transformer : transformers)
		{
			joins.add(new Conductor(transformer.primaryPositive(), transformer.primaryNegative(), Ohms.ZERO));
			joins.add(new Conductor(transformer.secondaryPositive(), transformer.secondaryNegative(), Ohms.ZERO));
		}

		NodeContraction galvanic = NodeContraction.of(nodes, joins);
		List<List<NodeId>> groups = new ArrayList<>();
		for (NodeId representative : galvanic.merged())
		{
			groups.add(nodes.stream()
				.filter(node -> galvanic.representativeOf(node).equals(representative))
				.toList());
		}

		return groups;
	}

	/** Checks that a kind of element appears no more than once, so that a branch names one thing. */
	private static void requireDistinct(List<?> elements, String kind)
	{
		if (new HashSet<>(elements).size() != elements.size())
		{
			throw new IllegalArgumentException("A circuit carries each " + kind
				+ " once; two alike between the same nodes are one branch");
		}
	}

	/** Checks that an element's nodes are nodes the circuit has. */
	private static void requireKnown(Set<NodeId> known, NodeId first, NodeId second)
	{
		for (NodeId node : List.of(first, second))
		{
			if (!known.contains(node))
			{
				throw new IllegalArgumentException(node + " is used by an element but is not a node of the circuit");
			}
		}
	}
}
