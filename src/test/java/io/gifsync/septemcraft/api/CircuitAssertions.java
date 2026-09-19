package io.gifsync.septemcraft.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What must hold of every solution, whatever circuit produced it. These say nothing about any one
 * circuit's answer, so they can be laid over all of them at once: a solver that balances current at
 * every node and accounts for every watt has got its signs, its orientations and its bookkeeping
 * right, whatever the numbers turn out to be.
 */
final class CircuitAssertions
{
	/** How far a sum of solved figures may stray from balancing before it is not balancing. */
	static final double BALANCED = 1.0e-7;

	private CircuitAssertions()
	{
	}

	/**
	 * Every current arriving at a node leaves it again, and every watt a machine delivers is drawn,
	 * burned in a conductor, or taken by a transformer on the way.
	 */
	static void assertConserves(Circuit circuit, Solution solution)
	{
		for (NodeId node : circuit.nodes())
		{
			double arriving = 0.0;
			for (Element element : circuit.elements())
			{
				if (element.to().equals(node))
				{
					arriving += solution.through(element).value();
				}

				if (element.from().equals(node))
				{
					arriving -= solution.through(element).value();
				}
			}

			assertEquals(0.0, arriving, BALANCED, "current does not balance at " + node);
		}

		double delivered = 0.0;
		for (Source source : circuit.sources())
		{
			delivered += solution.deliveredBy(source).value();
		}

		double accounted = 0.0;
		for (Load load : circuit.loads())
		{
			accounted += solution.drawnBy(load).value();
		}

		for (Conductor conductor : circuit.conductors())
		{
			accounted += solution.lostIn(conductor).value();
		}

		for (Transformer transformer : circuit.transformers())
		{
			accounted += solution.arrivingAt(transformer).value() - solution.leaving(transformer).value();
		}

		assertEquals(delivered, accounted, BALANCED, "power delivered is not power accounted for");
	}

	/**
	 * No reading anywhere in the solution is a figure that cannot be shown or reasoned with. A
	 * circuit the solver could not answer reads as zero throughout rather than as nothing at all.
	 */
	static void assertReadable(Circuit circuit, Solution solution)
	{
		for (NodeId node : circuit.nodes())
		{
			assertFinite(solution.voltageAt(node).value(), "the potential at " + node);
		}

		for (Element element : circuit.elements())
		{
			assertFinite(solution.through(element).value(), "the current through " + element);
		}

		for (Conductor conductor : circuit.conductors())
		{
			assertFinite(solution.lostIn(conductor).value(), "the power lost in " + conductor);
		}

		for (Source source : circuit.sources())
		{
			assertFinite(solution.deliveredBy(source).value(), "the power delivered by " + source);
		}

		for (Load load : circuit.loads())
		{
			assertFinite(solution.drawnBy(load).value(), "the power drawn by " + load);
		}

		for (Transformer transformer : circuit.transformers())
		{
			assertFinite(solution.arrivingAt(transformer).value(), "the power arriving at " + transformer);
			assertFinite(solution.leaving(transformer).value(), "the power leaving " + transformer);
		}
	}

	/** The total a line is burning between all of its conductors. */
	static double lineLoss(Circuit circuit, Solution solution)
	{
		double total = 0.0;
		for (Conductor conductor : circuit.conductors())
		{
			total += solution.lostIn(conductor).value();
		}

		return total;
	}

	private static void assertFinite(double reading, String what)
	{
		assertTrue(Double.isFinite(reading), what + " is " + reading);
	}
}
