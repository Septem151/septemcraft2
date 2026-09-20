package io.gifsync.septemcraft.api;

/**
 * One side of a transformer. Current runs in at {@link #from()}, the live terminal, and out at
 * {@link #to()}, so a primary taking power reads positive and a secondary giving it out reads
 * negative.
 */
public record Winding(ElementId id, NodeId from, NodeId to) implements Element
{
}
