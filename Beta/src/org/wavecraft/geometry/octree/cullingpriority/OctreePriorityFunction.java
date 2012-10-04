package org.wavecraft.geometry.octree.cullingpriority;

import org.wavecraft.geometry.octree.Octree;

public interface OctreePriorityFunction {
	public double priority(Octree node);
}
