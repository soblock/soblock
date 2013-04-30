package org.wavecraft.geometry.blocktree.modif;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Blocktree.State;


public class BlocktreeGrabber {	
	
	public static Blocktree nearestIntersectedLeaf(Blocktree root, Coord3d origin, Coord3d vector){
		MinAndArgmin best = nearestIntersectedLeafInner(root, origin, vector);
		
		return best.octree;
	}
	
	private static class MinAndArgmin{
		public Blocktree octree;
		public double dmin;
		public MinAndArgmin(Blocktree octree,double associatedDistance) {
			this.octree = octree;
			this.dmin = associatedDistance;
		}
	}
	
	private static MinAndArgmin nearestIntersectedLeafInner(Blocktree node, Coord3d origin, Coord3d vector){
		MinAndArgmin best = new MinAndArgmin(null, 10E20);
		if (node.doesIntersectLine(origin, vector)){
			if (node.hasSons()){
				Blocktree[] sons = node.getSons();
				for (int k=0 ;k<8;k++){
					MinAndArgmin minAndArgmin = nearestIntersectedLeafInner(sons[k], origin,vector);
					if (minAndArgmin.dmin < best.dmin){
						best.octree = minAndArgmin.octree;
						best.dmin = minAndArgmin.dmin;
					}
				}
			}
			else {
				if (node.getState() == State.LEAF){
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
