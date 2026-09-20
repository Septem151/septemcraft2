package io.gifsync.septemcraft.api;

/**
 * Which element in a circuit this is. The builder mints these as it assembles a circuit, and they
 * are what tells two elements apart when everything else about them agrees. A built grid really
 * does hold two runs of the same conductor between the same pair of nodes, and a solution reads
 * each of those runs on its own.
 */
public record ElementId(int index)
{
}
