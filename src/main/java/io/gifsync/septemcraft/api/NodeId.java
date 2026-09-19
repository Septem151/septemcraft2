package io.gifsync.septemcraft.api;

/**
 * A point in a circuit that elements join at. Minted by the builder that assembles the circuit, so
 * that whatever walks a world of blocks keeps its own map from a position to the node there.
 */
public record NodeId(int index)
{
}
