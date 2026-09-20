package io.gifsync.septemcraft.api;

/** A run of conductor between two nodes, which may resist nothing at all. */
public record Conductor(ElementId id, NodeId from, NodeId to, Ohms resistance) implements Element
{
}
