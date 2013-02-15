package org.wavecraft.geometry.octree.cullingpriority;

import org.wavecraft.geometry.DyadicBlock;

public interface OctreePriorityFunction {
	public double priority(DyadicBlock block);
}
