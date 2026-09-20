package io.gifsync.septemcraft.api;

/**
 * A point in a circuit that elements join at. The builder mints these as it assembles a circuit, so
 * that whatever walks a world of blocks keeps its own map from a position to the node there.
 */
public record NodeId(int index)
{
}
