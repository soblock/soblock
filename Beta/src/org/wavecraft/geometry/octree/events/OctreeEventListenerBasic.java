package org.wavecraft.geometry.octree.events;

import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeState;
import org.wavecraft.geometry.octree.OctreeStateDead;
import org.wavecraft.geometry.octree.OctreeStateFatherCool;
import org.wavecraft.geometry.octree.OctreeStateFatherWorried;
import org.wavecraft.geometry.octree.OctreeStateGround;
import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.builder.OctreeBuilder;

public class OctreeEventListenerBasic implements OctreeEventListener{
	OctreeBuilder builder;
	boolean printEvent = false;
	@Override
	public void handle(OctreeEvent event) {
		if (printEvent) System.out.println(event.toString());		
		switch (event.getKindOf()) {
		case KILL:
			event.getOctree().setState(OctreeStateDead.getInstance());
			event.getOctree().killSons();
			Octree father = event.getOctree().getFather();
			if (father != null && father.getState() instanceof OctreeStateFatherCool){
				father.setState(OctreeStateFatherWorried.getInstance());
			}
			break;
		case KILLGROUND:
			event.getOctree().setState(OctreeStateGround.getInstance());
			event.getOctree().killSons();
			Octree father1 = event.getOctree().getFather();
			if (father1 != null && father1.getState() instanceof OctreeStateFatherCool){
				father1.setState(OctreeStateFatherWorried.getInstance());
			}
			break;
		case LEAFY:
			event.getOctree().killSons();
			event.getOctree().setState(OctreeStateLeaf.getInstance());
			break;
		case SPLIT:
			event.getOctree().initSons();
			event.getOctree().setState(OctreeStateFatherCool.getInstance());
			break;
		default:
			break;
		}	
	}
}
