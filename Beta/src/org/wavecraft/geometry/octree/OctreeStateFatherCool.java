package org.wavecraft.geometry.octree;

import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;

import org.wavecraft.geometry.octree.events.OctreeEventMediator;


// the cool father enjoys life and doesnt care about his children being dead or alive.
// he just make them do his own work and relax.
// he has to make sure he is not culled.
// singleton
public class OctreeStateFatherCool extends OctreeState{


	private static OctreeStateFatherCool state = null;
	public static OctreeStateFatherCool getInstance(){
		if (state==null){
			state = new OctreeStateFatherCool();
		}
		return state;
	}

//	@Override
//	public void update(Octree octree, OctreeBuilder builder) {
//
//		if (builder.cull(octree)){
//			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.LEAFY);
//			OctreeEventMediator.addEvent(event);
//		}
//		else {
//			// Some work ? hum... let my children handle it...
//			Octree[] sons = octree.getSons();
//
//			for (int offset = 0; offset<8; offset++){
//				sons[offset].getState().update(sons[offset], builder);
//			}
//		}
//
//	}

	@Override
	public float internalJob(Octree octree, OctreeBuilder builder) {
		// TODO Auto-generated method stub
		// make sure im not culled
		if (builder.cull(octree)){
			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.LEAFY);
			OctreeEventMediator.addEvent(event);
			return 1;
		}
		
		return 0;
	}

}
