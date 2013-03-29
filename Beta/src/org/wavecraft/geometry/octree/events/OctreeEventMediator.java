package org.wavecraft.geometry.octree.events;

import org.wavecraft.events.EventMediator;

//singleton
public class OctreeEventMediator extends EventMediator<OctreeEvent, OctreeEventListener>{
	private static OctreeEventMediator octreeEventMediator;
	
	private OctreeEventMediator(){
	}
	public static OctreeEventMediator getInstance(){
		if (octreeEventMediator == null){
			octreeEventMediator = new OctreeEventMediator();
		}
		return octreeEventMediator;
	}
	
	public static void addEvent(OctreeEvent event){
		octreeEventMediator.add(event);
	}
	
	public static void addListener(OctreeEventListener listener){
		octreeEventMediator.add(listener);
	}
	
	public static void notifyAllListener(){
		octreeEventMediator.notifyMyListeners();
	}
	
	public static int eventCount(){
		return octreeEventMediator.events.size() + octreeEventMediator.nextEvents.size();
	}
	
}
