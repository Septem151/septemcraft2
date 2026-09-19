package io.gifsync.septemcraft.electrification.circuit;

import java.util.Map;

/**
 * What a circuit was carrying when it was last solved: a voltage at every node and a current in
 * every branch. Everything that reports an electrical reading reads it from here and computes
 * nothing of its own, so two instruments on one branch cannot disagree.
 */
record Solution(SolutionStatus status, Map<NodeId, Volts> voltages, Map<Conductor, Amperes> conductorCurrents,
	Map<Source, Amperes> sourceCurrents, Map<Load, Amperes> loadCurrents,
	Map<Transformer, Amperes> transformerCurrents)
{
	/** Holds every reading against change, so a solution handed out cannot be edited behind the solver. */
	Solution
	{
		voltages = Map.copyOf(voltages);
		conductorCurrents = Map.copyOf(conductorCurrents);
		sourceCurrents = Map.copyOf(sourceCurrents);
		loadCurrents = Map.copyOf(loadCurrents);
		transformerCurrents = Map.copyOf(transformerCurrents);
	}

	/** The potential at a node, measured against the reference its own part of the circuit is held at. */
	Volts voltageAt(NodeId node)
	{
		return read(voltages, node, "node");
	}

	/** The potential between two nodes, from the first to the second. */
	Volts across(NodeId from, NodeId to)
	{
		return voltageAt(from).minus(voltageAt(to));
	}

	/** The current in a conductor, positive from its first node to its second. */
	Amperes through(Conductor conductor)
	{
		return read(conductorCurrents, conductor, "conductor");
	}

	/** The current a source delivers, negative when its own circuit is driving it instead. */
	Amperes through(Source source)
	{
		return read(sourceCurrents, source, "source");
	}

	/** The current a load draws. */
	Amperes through(Load load)
	{
		return read(loadCurrents, load, "load");
	}

	/** The current in a transformer's primary winding. */
	Amperes through(Transformer transformer)
	{
		return read(transformerCurrents, transformer, "transformer");
	}

	/** The current in a transformer's secondary winding, which is its primary's less the loss. */
	Amperes secondaryThrough(Transformer transformer)
	{
		return new Amperes(through(transformer).value() * transformer.loss().remainder()
			/ transformer.ratio().ratio());
	}

	/** The power a source is delivering, negative when its circuit is driving it instead. */
	Watts deliveredBy(Source source)
	{
		return across(source.positive(), source.negative()).times(through(source));
	}

	/** The power a load is drawing. */
	Watts drawnBy(Load load)
	{
		return across(load.from(), load.to()).times(through(load));
	}

	/** The power a conductor is burning. */
	Watts lostIn(Conductor conductor)
	{
		return conductor.lossAt(through(conductor));
	}

	/** The power arriving at a transformer's primary. */
	Watts arrivingAt(Transformer transformer)
	{
		return across(transformer.primaryPositive(), transformer.primaryNegative()).times(through(transformer));
	}

	/** The power leaving a transformer's secondary. */
	Watts leaving(Transformer transformer)
	{
		return across(transformer.secondaryPositive(), transformer.secondaryNegative())
			.times(secondaryThrough(transformer));
	}

	/** Reads a value the solve recorded, and refuses to invent one for something the circuit never held. */
	private static <K, V> V read(Map<K, V> readings, K subject, String kind)
	{
		V reading = readings.get(subject);
		if (reading == null)
		{
			throw new IllegalArgumentException("That " + kind + " is not one this solution was solved over");
		}

		return reading;
	}
}
