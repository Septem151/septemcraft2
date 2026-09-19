package io.gifsync.septemcraft.api;

/** The share of what crosses something that does not come out the other side. */
public record LossFraction(double fraction)
{
	/** A crossing that loses nothing. */
	public static final LossFraction NONE = new LossFraction(0.0);
}
