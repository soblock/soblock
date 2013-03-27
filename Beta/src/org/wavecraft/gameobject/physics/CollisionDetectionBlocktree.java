package org.wavecraft.gameobject.physics;

import java.util.ArrayList;
import java.util.List;

import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.blocktree.Blocktree;

public class CollisionDetectionBlocktree {


	// fast intersection
	public static List<Blocktree> intersectedLeaf(Blocktree root, BoundingBox box){
		ArrayList<Blocktree> intersectedLeafList = new ArrayList<Blocktree>();
		inner(intersectedLeafList,root, box);
		return intersectedLeafList;
	}

	private static void inner(List<Blocktree> currList, Blocktree node,BoundingBox box){

		switch (node.getState()) {
		case DEAD_GROUND : case LEAF :
			if (box.intersects(node)){
				currList.add(node);
			}
			break;

		case FATHER : case GRAND_FATHER : case PATRIARCH :
			if (box.intersects(node)){
				for (Blocktree son : node.getSons()){
					inner(currList, son, box);
				}
			}
		default:
			break;
		};
	}

}
