package io.gifsync.septemcraft.api;

/**
 * What one pass of a solve read: a potential at every point of the circuit and a current in every
 * element that carries one. Anything it holds no reading for reads as nothing, so a circuit that
 * was not solved is a reading of this with nothing in it rather than a reading that refuses to be
 * taken.
 *
 * <p>The readings are held by position - a potential at each node's own index and a current at each
 * element's - because a solution writes a full set of them once a pass and keeps only the last. Held
 * against identity instead, every pass would hash a key per node and a key per element into maps
 * that the next pass throws away.
 *
 * <p>Every power here is worked out from those two figures and no others, which is what makes the
 * books balance whatever became of the pass that took them.
 */
final class CircuitReadings implements Solution
{
	private final SolutionStatus status;

	/** What each node was read at, by that node's own index. */
	private final double[] voltages;

	/** What each element was read carrying, by that element's own index. */
	private final double[] currents;

	/**
	 * Takes what one pass read. Both arrays become this reading's own and are never handed back
	 * out, so whoever fills them lets go of them here; that is what holds a reading against change,
	 * in place of copying one per pass.
	 */
	CircuitReadings(SolutionStatus status, double[] voltages, double[] currents)
	{
		this.status = status;
		this.voltages = voltages;
		this.currents = currents;
	}

	/** A circuit reading as nothing anywhere, which is what one that was not solved reads as. */
	static CircuitReadings nothing(SolutionStatus status)
	{
		return new CircuitReadings(status, new double[0], new double[0]);
	}

	/** These same readings, read as the last pass of a solve that never settled. */
	CircuitReadings unsettled()
	{
		return new CircuitReadings(SolutionStatus.UNSETTLED, voltages, currents);
	}

	/** How many nodes these readings reach, which is every node of the circuit that was solved. */
	int nodes()
	{
		return voltages.length;
	}

	/** What the node at an index was read at, which is nothing where these readings do not reach it. */
	double voltage(int node)
	{
		return node >= 0 && node < voltages.length ? voltages[node] : 0.0;
	}

	@Override
	public SolutionStatus status()
	{
		return status;
	}

	@Override
	public Volts voltageAt(NodeId node)
	{
		return new Volts(voltage(node.index()));
	}

	@Override
	public Volts across(NodeId from, NodeId to)
	{
		return voltageAt(from).minus(voltageAt(to));
	}

	@Override
	public Amperes through(Element element)
	{
		return new Amperes(current(element.id().index()));
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

	/** What the element at an index was read carrying, which is nothing where it carries none. */
	private double current(int element)
	{
		return element >= 0 && element < currents.length ? currents[element] : 0.0;
	}
}
