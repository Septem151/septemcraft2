package io.gifsync.septemcraft.api;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks what holds of every answer the solver gives, whatever circuit it was asked about. A
 * circuit worked out by hand pins one answer; these pin the rules every answer obeys, which is what
 * catches a sign taken the wrong way, a loss counted twice, or a branch written into the wrong
 * place, on circuits nobody thought to write down.
 */
class ConservationTest
{
	private static final double CLOSE = 1.0e-6;

	/** Every circuit the tests are written around, so that the rules below are laid over all of them. */
	static Stream<Arguments> everyFixture()
	{
		return Stream.of(
			Arguments.of("a resistive divider", (Supplier<Circuit>) () -> Fixtures.divider().circuit()),
			Arguments.of("a divider with a bolted joint",
				(Supplier<Circuit>) () -> Fixtures.jointedDivider(Ohms.ZERO).circuit()),
			Arguments.of("a divider with a resisting joint",
				(Supplier<Circuit>) () -> Fixtures.jointedDivider(new Ohms(2.0)).circuit()),
			Arguments.of("two conductors side by side", (Supplier<Circuit>) () -> Fixtures.parallelPair().circuit()),
			Arguments.of("an unbalanced bridge", (Supplier<Circuit>) () -> Fixtures.bridge().circuit()),
			Arguments.of("a closed loop of busbar", (Supplier<Circuit>) () -> Fixtures.ring(true).circuit()),
			Arguments.of("a loop of busbar cut on one side",
				(Supplier<Circuit>) () -> Fixtures.ring(false).circuit()),
			Arguments.of("a long feed to a device holding its power",
				(Supplier<Circuit>) () -> Fixtures.singleFeed().circuit()),
			Arguments.of("the same feed stepped up",
				(Supplier<Circuit>) () -> Fixtures.steppedFeed(new TurnsRatio(4.0)).circuit()),
			Arguments.of("a transformer that loses a share",
				(Supplier<Circuit>) () -> Fixtures.coupled(new TurnsRatio(2.0), new LossFraction(0.05)).circuit()),
			Arguments.of("two machines on one bus", (Supplier<Circuit>) () -> Fixtures.twoMachines().circuit()),
			Arguments.of("a live circuit beside a dead island", (Supplier<Circuit>) () -> Fixtures.island().circuit()));
	}

	/** Current balances at every node, and every watt delivered is drawn, burned or taken on the way. */
	@ParameterizedTest(name = "{0}")
	@MethodSource("everyFixture")
	void nothingIsCreatedOrDestroyed(String name, Supplier<Circuit> fixture)
	{
		Circuit circuit = fixture.get();

		Solution solution = new CircuitSolver().solve(circuit);

		CircuitAssertions.assertConserves(circuit, solution);
	}

	/**
	 * Every reading is a figure that can be shown on a gauge and reasoned with. A potential that is
	 * not a number says nothing about where it came from and spreads to everything derived from it.
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("everyFixture")
	void noReadingIsOutsideTheNumbers(String name, Supplier<Circuit> fixture)
	{
		Circuit circuit = fixture.get();

		Solution solution = new CircuitSolver().solve(circuit);

		CircuitAssertions.assertReadable(circuit, solution);
	}

	/**
	 * The circuits above are the ones somebody thought of. These are not: each is an arbitrary
	 * network with arbitrary devices on it, some of its joins resisting nothing. Whatever the solver
	 * makes of one, it must be able to account for it.
	 */
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
		26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50})
	void anyCircuitTheSolverAnswersIsOneItCanAccountFor(int seed)
	{
		Circuit circuit = Fixtures.arbitrary(new Random(seed));

		Solution solution = new CircuitSolver().solve(circuit);

		CircuitAssertions.assertReadable(circuit, solution);
		if (solution.status() == SolutionStatus.SOLVED)
		{
			CircuitAssertions.assertConserves(circuit, solution);
		}
	}

	/**
	 * The same circuit described in a different order is the same circuit. A solver that lets the
	 * order it happened to walk its own working reach the answer gives a grid that flickers between
	 * ticks while nothing about it has changed.
	 */
	@Test
	void theSameCircuitDescribedInAnyOrderSolvesTheSameWay()
	{
		CircuitBuilder one = new CircuitBuilder();
		NodeId reference = one.node();
		NodeId live = one.node();
		NodeId tap = one.node();
		one.source(reference, live, new Volts(12.0), Ohms.ZERO);
		one.conductor(live, tap, new Ohms(8.0));
		one.load(tap, reference, LoadClass.CONSTANT_RESISTANCE, new Watts(36.0), new Volts(12.0));

		CircuitBuilder other = new CircuitBuilder();
		NodeId otherReference = other.node();
		NodeId otherLive = other.node();
		NodeId otherTap = other.node();
		other.load(otherTap, otherReference, LoadClass.CONSTANT_RESISTANCE, new Watts(36.0), new Volts(12.0));
		other.conductor(otherLive, otherTap, new Ohms(8.0));
		other.source(otherReference, otherLive, new Volts(12.0), Ohms.ZERO);

		CircuitSolver solver = new CircuitSolver();

		assertEquals(solver.solve(one.build()).voltageAt(tap).value(),
			solver.solve(other.build()).voltageAt(otherTap).value(), 0.0);
	}

	/**
	 * A line burns the square of the current in it, so carrying the same power at four times the
	 * potential carries a quarter of the current and burns about a sixteenth of what it did. What
	 * the line resists per block may be retuned at any time; that stepping it up cuts the loss by
	 * something near the square of the ratio will not be.
	 */
	@Test
	void steppingALineUpBurnsFarLessOfIt()
	{
		CircuitSolver solver = new CircuitSolver();

		Circuit direct = Fixtures.singleFeed().circuit();
		Circuit stepped = Fixtures.steppedFeed(new TurnsRatio(4.0)).circuit();

		double burnedDirectly = CircuitAssertions.lineLoss(direct, solver.solve(direct));
		double burnedStepped = CircuitAssertions.lineLoss(stepped, solver.solve(stepped));

		assertTrue(burnedStepped > 0.0, "the stepped line burns nothing at all");
		assertTrue(burnedStepped < burnedDirectly / 8.0,
			"stepping up burned " + burnedStepped + " where running direct burned " + burnedDirectly);
	}

	/** The same device behind a matched pair of transformers still gets what it was rated for. */
	@Test
	void steppingALineUpStillDeliversWhatTheDeviceAsksFor()
	{
		Fixtures.SteppedFeed stepped = Fixtures.steppedFeed(new TurnsRatio(4.0));

		Solution solution = new CircuitSolver().solve(stepped.circuit());

		assertEquals(SolutionStatus.SOLVED, solution.status());
		assertEquals(Fixtures.RATING.value(), solution.drawnBy(stepped.load()).value(), CLOSE);
	}
}
