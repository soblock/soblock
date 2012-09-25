package org.wavecraft.geometry.octree.builder;

import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.FluidTree.FluidTree;


public interface OctreeBuilder{
	
	public abstract boolean cull(Octree octree);
	public abstract boolean cull(FluidTree fluitree);
	public abstract boolean isOutsideDomainOfInterest(Octree octree);
	public abstract boolean isGround(Octree octree);
	public abstract boolean isUpperFace(Face face);
	public abstract void setContent(Octree octree);
	public abstract double priority(Octree octree);
	
	
	
	
	
}
