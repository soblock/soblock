package org.wavecraft.geometry.octree.events;


public class OctreeEventUtils {
	public static String toString(OctreeEvent event){
		return String.format("%s %s", event.getClass().getName(),event.getOctree().toString());
	}
}
