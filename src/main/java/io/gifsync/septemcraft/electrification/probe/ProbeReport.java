package io.gifsync.septemcraft.electrification.probe;

import io.gifsync.septemcraft.api.Amperes;
import io.gifsync.septemcraft.api.Conductor;
import io.gifsync.septemcraft.api.Ohms;
import io.gifsync.septemcraft.api.Rpm;
import io.gifsync.septemcraft.api.Solution;
import io.gifsync.septemcraft.api.SolutionStatus;
import io.gifsync.septemcraft.api.Volts;
import io.gifsync.septemcraft.api.Watts;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * What the probe prints: the shaft it was held against, what became of the solve, and every reading
 * the solve took off the circuit that shaft drove.
 */
record ProbeReport(Rpm speed, ProbeCircuit probed, Solution solution)
{
	/** How many figures past the point a reading is printed to. */
	private static final String READING = "%.2f";

	/** How many figures past the point a shaft speed is printed to, a shaft being the coarser figure. */
	private static final String SPEED = "%.1f";

	/** The lines the probe prints, in the order they are read off the circuit. */
	List<Component> lines()
	{
		return List.of(
			reading("Shaft", format(SPEED, speed.value()) + " RPM, driving " + volts(speed.driving())),
			status(),
			reading("Line", ProbeCircuit.run().count() + " blocks out and back, "
				+ ohms(probed.lineResistance())),
			reading("Far end", volts(solution.across(probed.farLive(), probed.farReturn())) + ", floor "
				+ volts(probed.device().minimumVoltage())),
			reading("Current", amperes(solution.through(probed.line().get(0)))),
			reading("Burned in the line", watts(lost())),
			reading("Drawn by the device", watts(solution.drawnBy(probed.device()))),
			reading("Taken from the machine", watts(solution.deliveredBy(probed.machine()))));
	}

	/** What became of the solve, coloured by whether the circuit answered at all. */
	private Component status()
	{
		return Component.literal("Status: ")
			.append(Component.literal(solution.status().name()).withStyle(colourOf(solution.status())));
	}

	/** The power the whole line burned, which is what every run of it burned added up. */
	private Watts lost()
	{
		double burned = 0.0;
		for (Conductor run : probed.line())
		{
			burned += solution.lostIn(run).value();
		}

		return new Watts(burned);
	}

	/** How a status reads at a glance: settled, dark, still moving, or a fault to go and find. */
	private static ChatFormatting colourOf(SolutionStatus status)
	{
		return switch (status)
		{
			case SOLVED -> ChatFormatting.GREEN;
			case DE_ENERGISED -> ChatFormatting.GRAY;
			case UNSETTLED -> ChatFormatting.YELLOW;
			case SHORTED, SINGULAR, TOO_LARGE -> ChatFormatting.RED;
		};
	}

	/** One reading, named and printed after its name. */
	private static Component reading(String name, String value)
	{
		return Component.literal(name + ": ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(value).withStyle(ChatFormatting.WHITE));
	}

	private static String volts(Volts potential)
	{
		return format(READING, potential.value()) + " V";
	}

	private static String amperes(Amperes current)
	{
		return format(READING, current.value()) + " A";
	}

	private static String watts(Watts power)
	{
		return format(READING, power.value()) + " W";
	}

	private static String ohms(Ohms resistance)
	{
		return format(READING, resistance.value()) + " ohms";
	}

	/** A figure printed the same way wherever the world is being played. */
	private static String format(String figures, double value)
	{
		return String.format(Locale.ROOT, figures, value);
	}
}
