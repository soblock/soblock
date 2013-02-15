package org.wavecraft.geometry.octree.builder;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.octree.Octree;


public interface OctreeBuilder{
	
	public abstract boolean cull(Octree octree);
	public abstract boolean isOutsideDomainOfInterest(Octree octree);
	public abstract boolean isGround(Octree octree);
	public abstract boolean isUpperFace(Face face);
	public abstract void setContent(Octree octree); // DEPRECATED : TODO : remove and replace with contentAt
	public abstract int contentAt(DyadicBlock block);
	public abstract double priority(DyadicBlock block);
	
	
	
	
	
}
