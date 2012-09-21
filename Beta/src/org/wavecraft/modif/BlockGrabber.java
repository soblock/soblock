package org.wavecraft.modif;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.OctreeStateNotYetVisited;



public class BlockGrabber {

	
	
	public static Octree nearestIntersectedLeaf(Octree root, Coord3d origin, Coord3d vector){
		MinAndArgmin best = nearestIntersectedLeafInner(root, origin, vector);
		return best.octree;
	}
	
	private static class MinAndArgmin{
		public Octree octree;
		public double dmin;
		public MinAndArgmin(Octree octree,double associatedDistance) {
			this.octree = octree;
			this.dmin = associatedDistance;
		}
	}
	
	private static MinAndArgmin nearestIntersectedLeafInner(Octree node, Coord3d origin, Coord3d vector){
		MinAndArgmin best = new MinAndArgmin(null, 10E20);
		if (node.doesIntersectLine(origin, vector)){
			if (node.hasSons()){
				Octree[] sons = node.getSons();
				for (int k=0 ;k<8;k++){
					MinAndArgmin minAndArgmin = nearestIntersectedLeafInner(sons[k], origin,vector);
					if (minAndArgmin.dmin < best.dmin){
						best.octree = minAndArgmin.octree;
						best.dmin = minAndArgmin.dmin;
					}
				}
			}
			else {
				if ((node.getState() instanceof OctreeStateLeaf) || 
						(node.getState() instanceof OctreeStateNotYetVisited)){
					double d = node.nearestIntersectedFaceDistance(origin, vector);
					if (d>0 || d<1E20){
						best.dmin = d;
						best.octree = node;
					}
				}
			}
		}
		return best;
	}
}
