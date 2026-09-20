package io.gifsync.septemcraft.electrification.probe;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.gifsync.septemcraft.api.CircuitSolver;
import io.gifsync.septemcraft.api.Rpm;
import io.gifsync.septemcraft.api.Solution;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * A handheld probe. Held against anything of Create's that turns, it reads that block's speed,
 * drives a circuit with it, solves that circuit, and prints every reading to whoever is holding it.
 */
final class CircuitProbeItem extends Item
{
	/** What the probe says of a block that turns nothing, there being no speed to drive a circuit with. */
	private static final String NOTHING_TURNING = "Nothing kinetic here.";

	private final CircuitSolver solver;

	/** Builds the probe around a solver sized for the circuit it drives. */
	CircuitProbeItem(Item.Properties properties)
	{
		super(properties);
		solver = new CircuitSolver();
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Level level = context.getLevel();
		Optional<Player> holder = Optional.ofNullable(context.getPlayer());

		return level.isClientSide() || holder.isEmpty()
			? InteractionResult.SUCCESS
			: read(level, context.getClickedPos(), holder.get());
	}

	/** Reads the block at a position and tells the holder what a circuit driven by it does. */
	private InteractionResult read(Level level, BlockPos position, Player holder)
	{
		Optional<Rpm> turning = speedAt(level, position);
		if (turning.isEmpty())
		{
			holder.sendSystemMessage(Component.literal(NOTHING_TURNING));

			return InteractionResult.CONSUME;
		}

		ProbeCircuit probed = ProbeCircuit.drivenAt(turning.get());
		Solution solution = solver.solve(probed.circuit());
		new ProbeReport(turning.get(), probed, solution).lines().forEach(holder::sendSystemMessage);

		return InteractionResult.CONSUME;
	}

	/** The speed the block at a position is turning at, where that block turns at all. */
	private static Optional<Rpm> speedAt(Level level, BlockPos position)
	{
		return Optional.ofNullable(level.getBlockEntity(position))
			.filter(KineticBlockEntity.class::isInstance)
			.map(KineticBlockEntity.class::cast)
			.map(kinetic -> new Rpm(kinetic.getSpeed()));
	}
}
