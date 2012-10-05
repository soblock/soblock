package org.wavecraft.geometry.octree.events;

import org.wavecraft.geometry.octree.Octree;



public class OctreeEvent {
	protected Octree octree;
	private OctreeEventKindof kindOf;
	
	public OctreeEvent(Octree octree, OctreeEventKindof kindOf){
		this.octree = octree;
		this.kindOf = kindOf;
	}
	
	public Octree getOctree(){
		return this.octree;
	}
	public OctreeEventKindof getKindOf(){
		return kindOf;
	}
	
//	public OctreeEvent(Octree octree){
//		this.octree = octree;
//	}
	
	@Override
	public String toString(){
		String eventStringType="";
		switch (kindOf) {
		case KILL:
			eventStringType = "KILL";
			break;
			
		case KILLGROUND:
			eventStringType = "KILLGROUND";
			break;

		case LEAFY:
			eventStringType = "LEAFY";
			break;

		case SPLIT:
			eventStringType = "SPLIT";
			break;

		default:
			break;
		}
		return "EVENT" + eventStringType + octree.toString();
	}
}
