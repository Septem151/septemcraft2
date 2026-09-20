package io.gifsync.septemcraft.api;

/**
 * A joint that resists nothing and the node hanging from it, which is the node the joint was first
 * reached across. Whatever the rest of the circuit takes from that node has to arrive along this
 * joint, so reading the outermost of them first works every joint's current out from the outside
 * inwards.
 */
record HangingJoint(NodeId node, Conductor joint)
{
	/** Checks that a joint hangs a node it actually reaches. */
	HangingJoint
	{
		if (!joint.from().equals(node) && !joint.to().equals(node))
		{
			throw new IllegalArgumentException(joint + " does not reach " + node + " to hang it");
		}
	}

	/** Whether the current arriving at the hanging node leaves it the way the joint is oriented. */
	boolean outwards()
	{
		return joint.from().equals(node);
	}

	/** The node at the other end of this joint, which is the one it hangs from. */
	NodeId anchor()
	{
		return outwards() ? joint.to() : joint.from();
	}
}
