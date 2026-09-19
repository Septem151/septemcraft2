package io.gifsync.septemcraft.api;

/** A single electrical reading, whatever it measures. */
public interface Quantity
{
	/** The reading itself, in this quantity's own unit. */
	double value();
}
