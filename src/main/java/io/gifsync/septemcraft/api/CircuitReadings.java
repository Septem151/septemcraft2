package io.gifsync.septemcraft.api;

import java.util.Map;

/**
 * What one pass of a solve read: a potential at every point of the circuit and a current in every
 * element that carries one. Anything it was not given reads as nothing, so a circuit that was not
 * solved is a reading of this with nothing in it rather than a reading that refuses to be taken.
 *
 * <p>Every power here is worked out from those two figures and no others, which is what makes the
 * books balance whatever became of the pass that took them.
 */
record CircuitReadings(SolutionStatus status, Map<NodeId, Volts> voltages, Map<ElementId, Amperes> currents)
	implements
		Solution
{
	/** Holds every reading against change, so a reading handed out cannot be edited behind the solver. */
	CircuitReadings
	{
		voltages = Map.copyOf(voltages);
		currents = Map.copyOf(currents);
	}

	@Override
	public Volts voltageAt(NodeId node)
	{
		return voltages.getOrDefault(node, Volts.ZERO);
	}

	@Override
	public Volts across(NodeId from, NodeId to)
	{
		return voltageAt(from).minus(voltageAt(to));
	}

	@Override
	public Amperes through(Element element)
	{
		return currents.getOrDefault(element.id(), Amperes.ZERO);
	}

	@Override
	public Watts lostIn(Conductor conductor)
	{
		return through(conductor).dissipatedIn(conductor.resistance());
	}

	@Override
	public Watts deliveredBy(Source source)
	{
		return across(source.to(), source.from()).times(through(source));
	}

	@Override
	public Watts drawnBy(Load load)
	{
		return across(load.from(), load.to()).times(through(load));
	}

	@Override
	public Watts arrivingAt(Transformer transformer)
	{
		Winding primary = transformer.primary();

		return across(primary.from(), primary.to()).times(through(primary));
	}

	@Override
	public Watts leaving(Transformer transformer)
	{
		Winding secondary = transformer.secondary();

		return across(secondary.to(), secondary.from()).times(through(secondary));
	}
}
