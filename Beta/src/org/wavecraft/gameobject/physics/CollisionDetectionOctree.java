package org.wavecraft.gameobject.physics;

import java.util.ArrayList;

import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.octree.Octree;

import org.wavecraft.geometry.octree.OctreeStateFatherCool;
import org.wavecraft.geometry.octree.OctreeStateFatherWorried;
import org.wavecraft.geometry.octree.OctreeStateGround;
import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.OctreeStateNotYetVisited;



public class CollisionDetectionOctree {


	// fast intersection
	public static ArrayList<Octree> intersectedLeaf(Octree root, BoundingBox box){
		ArrayList<Octree> intersectedLeafList = new ArrayList<Octree>();
		inner(intersectedLeafList,root, box);
		return intersectedLeafList;
	}

	private static void inner(ArrayList<Octree> currList, Octree node,BoundingBox box){

		if (node.getState() instanceof OctreeStateLeaf ||
				node.getState() instanceof OctreeStateGround ||
				node.getState() instanceof OctreeStateNotYetVisited){
			if (box.intersects(node)){
				currList.add(node);
			}
		}
		else{
			if (node.getState() instanceof OctreeStateFatherCool ||
					node.getState() instanceof OctreeStateFatherWorried){
				if (box.intersects(node)){
					Octree[] sons = node.getSons();
					for (int offset = 0; offset<8; offset++){
						inner(currList,sons[offset], box);
					}
				}
			}
		}
	}
}
