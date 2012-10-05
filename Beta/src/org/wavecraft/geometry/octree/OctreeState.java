package org.wavecraft.geometry.octree;

import org.wavecraft.geometry.octree.builder.OctreeBuilder;

// OctreeState.update tells us what to do when we update the octree
// all important modification should be send to the event mediator
// so that each component of the application can update its representation
// of the octree


public abstract class OctreeState {

	public abstract float internalJob(Octree octree, OctreeBuilder builder);
	// the reurned value represent the 'computational cost' of the internal job.

	public static void update(Octree octree, OctreeBuilder builder){
		octree.getState().internalJob(octree, builder);
		if (octree.hasSons()){
			for (int i = 0; i<8 ; i++){
				update(octree.sons[i], builder);
			}
		}
	}

	

	//private static Octree partialUpdateInner(Octree,)
}
