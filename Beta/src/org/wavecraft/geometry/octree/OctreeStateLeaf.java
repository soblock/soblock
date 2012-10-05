package org.wavecraft.geometry.octree;

import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;


// the leaf has to be sure it stay leaf
//singleton
public class OctreeStateLeaf extends OctreeState{

	private static OctreeStateLeaf state = null;
	public static OctreeStateLeaf getInstance(){
		if (state==null){
			state = new OctreeStateLeaf();
		}
		return state;
	}



	@Override
	public float internalJob(Octree octree, OctreeBuilder builder) {
		// the culling condition may have changed
		if (!builder.cull(octree)){
			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.SPLIT);
			OctreeEventMediator.addEvent(event);
			return 1;
		}
		else {
			return 0;
		}

	}

}
