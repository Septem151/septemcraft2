package io.gifsync.septemcraft.electrification.circuit;

/**
 * Two windings coupled by a core, joining a primary node pair to a secondary pair. The secondary
 * sits at the primary's potential times the ratio and carries the primary's current divided by it,
 * less the loss, so the power leaving is the power arriving less exactly one deduction. Its current
 * is the primary current, positive when it flows into the primary's positive terminal.
 */
record Transformer(NodeId primaryPositive, NodeId primaryNegative, NodeId secondaryPositive,
	NodeId secondaryNegative, TurnsRatio ratio, LossFraction loss)
{
	/** Checks that each winding sits between two different nodes. */
	Transformer
	{
		if (primaryPositive.equals(primaryNegative))
		{
			throw new IllegalArgumentException("A primary winding sits between two nodes, not " + primaryPositive
				+ " and itself");
		}

		if (secondaryPositive.equals(secondaryNegative))
		{
			throw new IllegalArgumentException("A secondary winding sits between two nodes, not " + secondaryPositive
				+ " and itself");
		}
	}

	/** The power this transformer delivers out of its secondary for a power arriving at its primary. */
	Watts delivers(Watts arriving)
	{
		return arriving.lessLoss(loss);
	}
}
