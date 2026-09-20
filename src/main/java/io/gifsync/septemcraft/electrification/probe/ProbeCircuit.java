package io.gifsync.septemcraft.electrification.probe;

import io.gifsync.septemcraft.api.Blocks;
import io.gifsync.septemcraft.api.Circuit;
import io.gifsync.septemcraft.api.CircuitBuilder;
import io.gifsync.septemcraft.api.Conductor;
import io.gifsync.septemcraft.api.ConductorForm;
import io.gifsync.septemcraft.api.Load;
import io.gifsync.septemcraft.api.LoadClass;
import io.gifsync.septemcraft.api.NodeId;
import io.gifsync.septemcraft.api.Ohms;
import io.gifsync.septemcraft.api.Rpm;
import io.gifsync.septemcraft.api.Source;
import io.gifsync.septemcraft.api.Volts;
import io.gifsync.septemcraft.api.Watts;
import java.util.List;

/**
 * The circuit the probe drives with whatever shaft it was held against: a machine turning at that
 * shaft's speed, a long run of catenary wire out and back again, and one device holding its power
 * at the far end of it.
 *
 * <p>Every figure the run is built from is this probe's own and stands for no rule. They are
 * chosen so that a shaft held at 128 RPM reads 96 V at the far end, 341 W burned in the line and
 * 1,365 W taken from the machine, which is a line long enough to sag hard and short enough that
 * there is still an answer to find.
 */
record ProbeCircuit(Circuit circuit, Source machine, Load device, List<Conductor> line, NodeId farLive,
	NodeId farReturn, Ohms lineResistance)
{
	/** How far the probe runs its line, out and as many blocks back. */
	private static final Blocks RUN = new Blocks(150);

	/** The power the probe's device holds against whatever its supply does. */
	private static final Watts RATING = new Watts(1024.0);

	/** The potential the probe's device is rated at, which is the speed a shaft is usually held at. */
	private static final Volts NOMINAL = new Volts(128.0);

	/**
	 * The potential the probe's device gives up below. Half the rating's own potential, which is
	 * where the collapsed answer such a device also satisfies always sits below.
	 */
	private static final Volts FLOOR = new Volts(NOMINAL.value() / 2.0);

	/** How many arms the line is run as, being one out and one back. */
	private static final double ARMS = 2.0;

	/** Holds the line against change, so a circuit handed out cannot be edited behind the probe. */
	ProbeCircuit
	{
		line = List.copyOf(line);
	}

	/** The probe's circuit, driven by a machine turning at the speed named. */
	static ProbeCircuit drivenAt(Rpm speed)
	{
		Ohms arm = ConductorForm.CATENARY_WIRE.resistanceOver(RUN);

		CircuitBuilder builder = new CircuitBuilder();
		NodeId reference = builder.node();
		NodeId live = builder.node();
		NodeId farLive = builder.node();
		NodeId farReturn = builder.node();

		Source machine = builder.source(reference, live, speed.driving(), Ohms.ZERO);
		Conductor out = builder.conductor(live, farLive, arm);
		Conductor back = builder.conductor(farReturn, reference, arm);
		Load device = builder.load(farLive, farReturn, LoadClass.CONSTANT_POWER, RATING, NOMINAL, FLOOR);

		return new ProbeCircuit(builder.build(), machine, device, List.of(out, back), farLive, farReturn,
			new Ohms(ARMS * arm.value()));
	}

	/**
	 * The least potential a machine must drive for this circuit to answer. A device holding its power
	 * behind a line satisfies the circuit at two potentials at once, and the higher of the two is what
	 * the line really runs at; this is where that higher one lands exactly on the device's floor.
	 * Driven any slower the device collapses the line, gives up, recovers and collapses it again, and
	 * there is no potential the circuit settles at.
	 *
	 * <p>A device with no floor at all gives up nowhere, so what is left is the shorter question of
	 * whether the two potentials exist to choose between. The floor never asks for less than that.
	 */
	Volts leastDriving()
	{
		double held = device.ratedPower().value() * lineResistance.value();
		double floor = device.minimumVoltage().value();

		return new Volts(floor > 0.0 ? floor + held / floor : 2.0 * Math.sqrt(held));
	}

	/** How long the run is, which is the same length out as back. */
	static Blocks run()
	{
		return RUN;
	}
}
