package io.gifsync.septemcraft.api;

/** A resistance. */
public record Ohms(double value)
{
	/** No resistance at all, which is what a bolted joint between two blocks offers. */
	public static final Ohms ZERO = new Ohms(0.0);
}
