package org.wavecraft.geometry.octree.builder;

import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;

public class OctreeUpdaterPartial implements OctreeUpdater{
	private Octree octree;
	private OctreeBuilder builder;
	private Octree lastPosition;
	private float budgetPerFrame;
	
	
	public OctreeUpdaterPartial(Octree octree,OctreeBuilder builder){
		this.octree = octree;
		lastPosition = octree;
		this.builder = builder;
		budgetPerFrame = 256;
	}
	
	
	
	
	// return either the octree or the computational budget left.
	private static Object partialUpdate(Octree octree, OctreeBuilder builder, Float budget){
		//System.out.println(budget);
		if (budget < 0 ){
			return octree; // I have used all my budget for update. I must stop now.
		}
		budget = budget - octree.getState().internalJob(octree, builder);
		if (octree.hasSons()){
			for (int i =0; i<8;i++){
				Object result = partialUpdate(octree.getSons()[i], builder, budget);
				if (result instanceof Float){
					budget = (Float) result;
				}
				if (result instanceof Octree){
					return octree.getSons()[i];
				}
			}
		}
		return budget;
	}

	private static Object resumePartialUpdate(Octree octree, int offset, OctreeBuilder builder, Float budget){
		// call my sons only from offset to 8 
		if (budget < 0 ){
			return octree; // I have used all my budget for update. I must stop now.
		}
		if (offset == -1){
			budget = budget - octree.getState().internalJob(octree, builder);
		}
		if (octree.hasSons()){
			for (int i =offset+1; i<8; i++){
				Object result = partialUpdate(octree.getSons()[i],  builder, budget);
				if (result instanceof Float){
					budget = (Float) result;
				}
				if (result instanceof Octree){
					return octree.getSons()[i];
				}
			}
		}
		int nextOffset = octree.offset();
		if (octree.getFather() != null){
			return resumePartialUpdate(octree.getFather(), nextOffset, builder, budget);
		}
		else {
			return null;
		}
	}


	@Override
	public void updateOctree() {
		Object result = resumePartialUpdate(lastPosition, -1, builder, new Float(budgetPerFrame));
		if (result instanceof Octree) {
			lastPosition = (Octree) result; 
		}
		if (result == null){
			lastPosition= octree;
		}
		OctreeEventMediator.notifyAllListener();
	}
}
