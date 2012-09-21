package org.wavecraft.geometry.octree;

import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;


// singleton
public class OctreeStateNotYetVisited extends OctreeState{
	private static OctreeStateNotYetVisited state = null;
	public static OctreeStateNotYetVisited getInstance(){
		if (state==null){
			state = new OctreeStateNotYetVisited();
		}
		return state;
	}

	@Override
	public float internalJob(Octree octree, OctreeBuilder builder) {
		if (builder.isOutsideDomainOfInterest(octree)){
			if (builder.isGround(octree)){
			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.KILLGROUND);
			OctreeEventMediator.addEvent(event);
			}
			else{
			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.KILL);
			OctreeEventMediator.addEvent(event);
			}
			
		}
		else{
			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.LEAFY);
			builder.setContent(octree);
			OctreeEventMediator.addEvent(event);
			return 0.01f;
		}
		return 1; //expensive operation
	}
	

}
