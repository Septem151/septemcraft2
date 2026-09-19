package io.gifsync.septemcraft.electrification.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Which node each of a circuit's nodes has been merged into. Two things bolted together are one
 * node, so a chain of conductors that resist nothing collapses to the first node in the chain -
 * which is also what keeps a resistance of zero from handing the solver a matrix it cannot invert.
 */
record NodeContraction(Map<NodeId, NodeId> representatives, List<NodeId> merged)
{
	/** Checks that every node is accounted for and that the merged nodes are the ones represented. */
	NodeContraction
	{
		representatives = Map.copyOf(representatives);
		merged = List.copyOf(merged);
	}

	/** Merges every chain of resistanceless conductors in a circuit into one node apiece. */
	static NodeContraction of(List<NodeId> nodes, List<Conductor> conductors)
	{
		Map<NodeId, NodeId> parents = new HashMap<>();
		for (NodeId node : nodes)
		{
			parents.put(node, node);
		}

		for (Conductor conductor : conductors)
		{
			if (conductor.resistance().isNone())
			{
				join(parents, conductor.from(), conductor.to());
			}
		}

		Map<NodeId, NodeId> representatives = new HashMap<>();
		for (NodeId node : nodes)
		{
			representatives.put(node, find(parents, node));
		}

		return new NodeContraction(representatives, new ArrayList<>(new LinkedHashSet<>(
			nodes.stream().map(representatives::get).toList())));
	}

	/** The node a node has been merged into, which is the node itself when nothing merged it. */
	NodeId representativeOf(NodeId node)
	{
		NodeId representative = representatives.get(node);
		if (representative == null)
		{
			throw new IllegalArgumentException(node + " is not a node of this circuit");
		}

		return representative;
	}

	/** Joins two nodes' chains, keeping whichever root came first in the circuit's own order. */
	private static void join(Map<NodeId, NodeId> parents, NodeId first, NodeId second)
	{
		NodeId one = find(parents, first);
		NodeId other = find(parents, second);
		if (one.equals(other))
		{
			return;
		}

		if (one.index() < other.index())
		{
			parents.put(other, one);
		}
		else
		{
			parents.put(one, other);
		}
	}

	/** Walks a node up to the root of its chain, flattening what it walks on the way. */
	private static NodeId find(Map<NodeId, NodeId> parents, NodeId node)
	{
		NodeId parent = parents.get(node);
		if (parent == null)
		{
			throw new IllegalArgumentException(node + " is not a node of this circuit");
		}

		if (parent.equals(node))
		{
			return node;
		}

		NodeId root = find(parents, parent);
		parents.put(node, root);
		return root;
	}
}
