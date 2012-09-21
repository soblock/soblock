package org.wavecraft.geometry.octree.cullingpriority;

import org.wavecraft.geometry.octree.Octree;

public interface Culler {
	public boolean cull(Octree octree);
}
