package io.gifsync.septemcraft.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The circuits the tests are written around. Each is handed back with the nodes and elements a test
 * needs to read it, so that nothing has to count indices to find what it just built.
 */
final class Fixtures
{
	/** The resistance a test gives a machine's own windings, which is the fixture's figure and no rule. */
	static final Ohms WINDING = new Ohms(0.5);

	/** The potential a test runs its machines at, which is the fixture's figure and no rule. */
	static final Volts NOMINAL = new Volts(128.0);

	/** The power a test rates its devices for, which is the fixture's figure and no rule. */
	static final Watts RATING = new Watts(1024.0);

	/**
	 * How much of the resistance a line could carry and still be answerable the feed fixtures use.
	 * Three quarters of it leaves the far end sagging to three quarters of the machine's potential,
	 * which is a line worth stepping up and not one about to collapse.
	 */
	private static final double LINE_SHARE_OF_ITS_CEILING = 0.75;

	/**
	 * A device that runs on whatever it is given. The fixtures that exist to pin a circuit's
	 * arithmetic use one, so that nothing switches off partway through and changes what is being
	 * measured.
	 */
	private static final Volts NO_FLOOR = Volts.ZERO;

	/**
	 * Where the devices on the feed fixtures give up. A device holding its power on a resistive
	 * line satisfies the circuit at two potentials, and those two always sit either side of half
	 * the machine's own - so a floor exactly there admits the answer the line really settles at and
	 * refuses the collapsed one, whatever the line is retuned to resist.
	 */
	private static final Volts FEED_FLOOR = new Volts(NOMINAL.value() / 2.0);

	private Fixtures()
	{
	}

	/** A twelve-volt divider: an eight-ohm arm feeding a load that would take the whole of it alone. */
	record Divider(Circuit circuit, NodeId reference, NodeId live, NodeId tap, Conductor arm, Source source,
		Load load)
	{
	}

	/** The same divider with its arm split in two by a joint, which may or may not resist anything. */
	record JointedDivider(Circuit circuit, NodeId reference, NodeId live, NodeId middle, NodeId tap,
		Conductor joint, Load load)
	{
	}

	/** A divider whose arm is two conductors side by side. */
	record ParallelPair(Circuit circuit, NodeId tap, Conductor wide, Conductor narrow, Source source)
	{
	}

	/** Four arms and a fifth conductor across the middle of them, which no folding of resistances answers. */
	record Bridge(Circuit circuit, NodeId leftCorner, NodeId rightCorner, Conductor across, Source source)
	{
	}

	/** A loop of busbar fed at one point and tapped at the point opposite. */
	record Ring(Circuit circuit, NodeId reference, NodeId feed, NodeId tap, Ohms edge)
	{
	}

	/** A machine feeding a load that holds its power, down a line long enough to sag. */
	record Feed(Circuit circuit, NodeId reference, NodeId live, NodeId farLive, NodeId farReturn, Source source,
		Load load, List<Conductor> line, Ohms lineResistance)
	{
		/** The potential the far end settles at, which is the upper of the two this circuit permits. */
		Volts stable()
		{
			return farEnd(1.0);
		}

		/** The potential the far end would sit at in collapse, which is the lower of the two. */
		Volts collapsed()
		{
			return farEnd(-1.0);
		}

		/**
		 * A device holding its power on a resistive line satisfies the circuit at two potentials,
		 * one either side of half the machine's own. Both are read off the fixture's own figures,
		 * so neither moves if what a block of wire resists is retuned.
		 */
		private Volts farEnd(double branch)
		{
			double driving = NOMINAL.value();
			double margin = 1.0 - 4.0 * RATING.value() * lineResistance.value() / (driving * driving);

			return new Volts(driving * (1.0 + branch * Math.sqrt(margin)) / 2.0);
		}
	}

	/** The same feed behind a matched pair of transformers, running the line above its machines. */
	record SteppedFeed(Circuit circuit, Load load, List<Conductor> line)
	{
	}

	/** A machine, a transformer, and a load on the far side of it. */
	record Coupled(Circuit circuit, NodeId secondaryLive, NodeId secondaryReturn, Transformer transformer,
		Source source, Load load)
	{
	}

	/** Two machines of unlike speed on one bus, and one load across it. */
	record TwoMachines(Circuit circuit, NodeId live, NodeId reference, Source fast, Source slow, Load load)
	{
	}

	/** A live circuit, and beside it a conductor joined to nothing that drives it. */
	record Island(Circuit circuit, NodeId adrift, NodeId alsoAdrift, Conductor stranded)
	{
	}

	/** A machine with a bolted path from one terminal back to the other. */
	record ShortCircuit(Circuit circuit, Source source, Conductor bolt)
	{
	}

	static Divider divider()
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId tap = builder.node();

		Source source = builder.source(reference, live, new Volts(12.0), Ohms.ZERO);
		Conductor arm = builder.conductor(live, tap, new Ohms(8.0));
		Load load = builder.load(tap, reference, LoadClass.CONSTANT_RESISTANCE, new Watts(36.0), new Volts(12.0),
			NO_FLOOR);

		return new Divider(builder.build(), reference, live, tap, arm, source, load);
	}

	/** The divider above, its arm split by a joint of the resistance named. */
	static JointedDivider jointedDivider(Ohms jointResistance)
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId middle = builder.node();
		NodeId tap = builder.node();

		builder.source(reference, live, new Volts(12.0), Ohms.ZERO);
		builder.conductor(live, middle, new Ohms(8.0));
		Conductor joint = builder.conductor(middle, tap, jointResistance);
		Load load = builder.load(tap, reference, LoadClass.CONSTANT_RESISTANCE, new Watts(36.0), new Volts(12.0),
			NO_FLOOR);

		return new JointedDivider(builder.build(), reference, live, middle, tap, joint, load);
	}

	static ParallelPair parallelPair()
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId tap = builder.node();

		Source source = builder.source(reference, live, new Volts(12.0), Ohms.ZERO);
		Conductor wide = builder.conductor(live, tap, new Ohms(3.0));
		Conductor narrow = builder.conductor(live, tap, new Ohms(6.0));
		builder.load(tap, reference, LoadClass.CONSTANT_RESISTANCE, new Watts(36.0), new Volts(12.0), NO_FLOOR);

		return new ParallelPair(builder.build(), tap, wide, narrow, source);
	}

	static Bridge bridge()
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId leftCorner = builder.node();
		NodeId rightCorner = builder.node();

		Source source = builder.source(reference, live, new Volts(12.0), Ohms.ZERO);
		builder.conductor(live, leftCorner, new Ohms(6.0));
		builder.conductor(live, rightCorner, new Ohms(3.0));
		builder.conductor(leftCorner, reference, new Ohms(3.0));
		builder.conductor(rightCorner, reference, new Ohms(6.0));
		Conductor across = builder.conductor(leftCorner, rightCorner, new Ohms(2.0));

		return new Bridge(builder.build(), leftCorner, rightCorner, across, source);
	}

	/**
	 * A four-cornered loop of busbar, each side six blocks of it, fed at one corner and tapped at
	 * the corner opposite by something drawing one ampere. Closed, the tap is fed down both sides at
	 * once; cut, down the one side left.
	 */
	static Ring ring(boolean closed)
	{
		Ohms edge = ConductorForm.BUSBAR.resistanceOver(new Blocks(6));

		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId feed = builder.node();
		NodeId leftCorner = builder.node();
		NodeId tap = builder.node();
		NodeId rightCorner = builder.node();

		builder.source(reference, feed, new Volts(1.0), Ohms.ZERO);
		builder.conductor(feed, leftCorner, edge);
		builder.conductor(leftCorner, tap, edge);
		builder.conductor(tap, rightCorner, edge);
		if (closed)
		{
			builder.conductor(rightCorner, feed, edge);
		}

		builder.load(tap, reference, LoadClass.CONSTANT_CURRENT, new Watts(1.0), new Volts(1.0), NO_FLOOR);

		return new Ring(builder.build(), reference, feed, tap, edge);
	}

	/**
	 * A machine feeding a load that holds its power, down a line of catenary wire out and as many
	 * blocks back. The line is long enough that the potential at the far end sags well below the
	 * machine's own, and short enough that there is still an answer.
	 */
	static Feed singleFeed()
	{
		Blocks length = feedLength();
		Ohms arm = ConductorForm.CATENARY_WIRE.resistanceOver(length);

		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId farLive = builder.node();
		NodeId farReturn = builder.node();

		Source source = builder.source(reference, live, NOMINAL, Ohms.ZERO);
		Conductor out = builder.conductor(live, farLive, arm);
		Conductor back = builder.conductor(farReturn, reference, arm);
		Load load = builder.load(farLive, farReturn, LoadClass.CONSTANT_POWER, RATING, NOMINAL, FEED_FLOOR);

		return new Feed(builder.build(), reference, live, farLive, farReturn, source, load, List.of(out, back),
			new Ohms(2.0 * arm.value()));
	}

	/**
	 * The most a line may resist and still leave a device holding its power an answer, which is the
	 * machine's own potential squared over four times that power. Past it the device satisfies the
	 * circuit at no potential whatever and there is nothing for a solver to find.
	 */
	static Ohms answerableCeiling(Volts driving, Watts held)
	{
		return new Ohms(driving.value() * driving.value() / (4.0 * held.value()));
	}

	/**
	 * How long a run the transmission fixtures use. The length is worked back from
	 * {@link #answerableCeiling(Volts, Watts)} rather than written down, so that retuning what a
	 * block of wire resists moves the run rather than quietly leaving the fixture unanswerable.
	 */
	private static Blocks feedLength()
	{
		double wanted = LINE_SHARE_OF_ITS_CEILING * answerableCeiling(NOMINAL, RATING).value();

		return new Blocks((int) Math.round(wanted / (2.0 * ConductorForm.CATENARY_WIRE.perBlock().value())));
	}

	/** The same machine, line and load, with the line run above the machines by a matched pair of transformers. */
	static SteppedFeed steppedFeed(TurnsRatio step)
	{
		Ohms arm = ConductorForm.CATENARY_WIRE.resistanceOver(feedLength());

		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId lineLive = builder.node();
		NodeId lineReturn = builder.node();
		NodeId farLineLive = builder.node();
		NodeId farLineReturn = builder.node();
		NodeId farLive = builder.node();
		NodeId farReturn = builder.node();

		builder.source(reference, live, NOMINAL, Ohms.ZERO);
		builder.transformer(builder.winding(live, reference), builder.winding(lineLive, lineReturn), step,
			LossFraction.NONE);
		Conductor out = builder.conductor(lineLive, farLineLive, arm);
		Conductor back = builder.conductor(farLineReturn, lineReturn, arm);
		builder.transformer(builder.winding(farLineLive, farLineReturn), builder.winding(farLive, farReturn),
			step.inverted(), LossFraction.NONE);
		Load load = builder.load(farLive, farReturn, LoadClass.CONSTANT_POWER, RATING, NOMINAL, FEED_FLOOR);

		return new SteppedFeed(builder.build(), load, List.of(out, back));
	}

	/** A machine, a transformer of the ratio and loss named, and a load across its secondary. */
	static Coupled coupled(TurnsRatio ratio, LossFraction loss)
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId secondaryLive = builder.node();
		NodeId secondaryReturn = builder.node();

		Source source = builder.source(reference, live, NOMINAL, Ohms.ZERO);
		Transformer transformer = builder.transformer(builder.winding(live, reference),
			builder.winding(secondaryLive, secondaryReturn), ratio, loss);
		Load load = builder.load(secondaryLive, secondaryReturn, LoadClass.CONSTANT_RESISTANCE, RATING,
			ratio.applyTo(NOMINAL), NO_FLOOR);

		return new Coupled(builder.build(), secondaryLive, secondaryReturn, transformer, source, load);
	}

	/** Two machines across one bus, one turning faster than the other, and a load between them. */
	static TwoMachines twoMachines()
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();

		Source fast = builder.source(reference, live, NOMINAL, WINDING);
		Source slow = builder.source(reference, live, new Volts(120.0), WINDING);
		Load load = builder.load(live, reference, LoadClass.CONSTANT_POWER, RATING, NOMINAL, NO_FLOOR);

		return new TwoMachines(builder.build(), live, reference, fast, slow, load);
	}

	/** A live circuit, and beside it two nodes joined to each other and to nothing else. */
	static Island island()
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId adrift = builder.node();
		NodeId alsoAdrift = builder.node();

		builder.source(reference, live, NOMINAL, Ohms.ZERO);
		builder.load(live, reference, LoadClass.CONSTANT_RESISTANCE, RATING, NOMINAL, NO_FLOOR);
		Conductor stranded = builder.conductor(adrift, alsoAdrift, new Ohms(4.0));

		return new Island(builder.build(), adrift, alsoAdrift, stranded);
	}

	/** A machine with a path back to itself that resists nothing, behind the windings named. */
	static ShortCircuit shortedThrough(Ohms windings)
	{
		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();

		Source source = builder.source(reference, live, NOMINAL, windings);
		Conductor path = builder.conductor(live, reference, Ohms.ZERO);

		return new ShortCircuit(builder.build(), source, path);
	}

	/**
	 * A circuit of no particular shape, built from the given source of randomness. Every node is
	 * joined to one already placed, so the whole of it hangs together; some of those joins resist
	 * nothing, which is what a run of bolted blocks amounts to.
	 */
	static Circuit arbitrary(Random random)
	{
		CircuitBuilder builder = new CircuitBuilder();
		List<NodeId> nodes = new ArrayList<>();
		nodes.add(builder.node());

		int count = 3 + random.nextInt(6);
		for (int index = 1; index < count; index++)
		{
			NodeId node = builder.node();
			NodeId already = nodes.get(random.nextInt(nodes.size()));
			builder.conductor(already, node,
				random.nextInt(4) == 0 ? Ohms.ZERO : new Ohms(0.1 + random.nextDouble() * 8.0));
			nodes.add(node);
		}

		for (int extra = random.nextInt(3); extra > 0; extra--)
		{
			NodeId from = nodes.get(random.nextInt(nodes.size()));
			NodeId to = nodes.get(random.nextInt(nodes.size()));
			if (!from.equals(to))
			{
				builder.conductor(from, to, new Ohms(0.1 + random.nextDouble() * 8.0));
			}
		}

		builder.source(nodes.get(0), nodes.get(1), new Volts(16.0 + random.nextDouble() * 240.0), WINDING);

		LoadClass[] classes = LoadClass.values();
		for (int drawing = 1 + random.nextInt(3); drawing > 0; drawing--)
		{
			builder.load(nodes.get(1 + random.nextInt(nodes.size() - 1)), nodes.get(0),
				classes[random.nextInt(classes.length)], new Watts(1.0 + random.nextDouble() * 512.0), NOMINAL,
				NO_FLOOR);
		}

		return builder.build();
	}
}
