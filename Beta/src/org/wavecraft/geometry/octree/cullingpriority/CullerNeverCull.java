package org.wavecraft.geometry.octree.cullingpriority;

import org.wavecraft.geometry.octree.Octree;

public class CullerNeverCull implements Culler{

	@Override
	public boolean cull(Octree octree) {
		return octree.getJ() == 0;
	}
	
}	
