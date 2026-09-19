package io.gifsync.septemcraft.api;

/**
 * Two windings coupled to each other. What arrives at the primary leaves the secondary at the
 * potential the turns ratio makes of it, less the one share the transformer takes on the way.
 */
public record Transformer(Winding primary, Winding secondary, TurnsRatio ratio, LossFraction loss)
{
}
