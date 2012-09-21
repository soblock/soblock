package org.wavecraft.geometry.octree;

import java.util.ArrayList;

import org.wavecraft.geometry.Coord3d;

public class OctreeUtils {
	public static ArrayList<Octree> bufferize(Octree root){
		ArrayList<Octree> list = new ArrayList<Octree>();
		bufferizeInner(list, root);
		return list;
	}

	private static void bufferizeInner(ArrayList<Octree> tmpList, Octree node){
		if (node.getState() instanceof OctreeStateLeaf ||
				node.getState() instanceof OctreeStateNotYetVisited){
			tmpList.add(node);
		}
		if (node.getState() instanceof OctreeStateFatherCool ||
				node.getState() instanceof OctreeStateFatherWorried){
			Octree[] sons = node.getSons();
			for (int offset = 0; offset < 8; offset ++){
				bufferizeInner(tmpList, sons[offset]);
			}
		}
	}
	
	
}
