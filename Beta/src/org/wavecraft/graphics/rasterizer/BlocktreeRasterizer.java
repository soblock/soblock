package org.wavecraft.graphics.rasterizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import static org.wavecraft.geometry.blocktree.Blocktree.State.*;

/**
 * 
 * @author laurentsifre
 * this class provides methods for rasterizing
 * a blocktree into a set of faces
 */
public class BlocktreeRasterizer {

	public static Set<Face> allNonDoublonFaceByCheckingDoublonInHashSet(Blocktree root){
		HashSet<Face> allFaces = new HashSet<Face>();
		putAllFaceInHashetInner(root, allFaces);
		return allFaces;
	}
	
	/**
	 * method of removing the doublon by checking if the reverse face is not in the hashset
	 * @param node
	 * @param allFaces
	 */
	private static void putAllFaceInHashetInner(Blocktree node, HashSet<Face> allFaces){
		switch (node.getState()) {
		case FATHER: case GRAND_FATHER:
			for (Blocktree son : node.getSons()){
				putAllFaceInHashetInner(son, allFaces);
			}
			break;

		case LEAF:
			for (Face face : node.getFaces()){
				putFaceInHashset(face, allFaces);
			}
		default:
			break;
		}
	}

	private static void putFaceInHashset(Face face, HashSet<Face> allFaces){
		Face reverse = face.reverse();
		if (allFaces.contains(reverse)){
			allFaces.remove(reverse);
		} else {
			allFaces.add(face);
		}
	}

	/**
	 * this method computes the set of all non double face by checking
	 * the state of the node in front of the face. slower than the method
	 * with hashset because we have to go through the tree but computes
	 * less faces.
	 * @param root
	 * @return
	 */
	public static List<Face> allNonDoublonFaceByCheckingNeigbhorsInTree(Blocktree root){
		List<Face> allFaces = new ArrayList<Face>();
		allNonDoublonFaceByCheckingNeigbhorsInTreeInner(root, root, allFaces);
		return allFaces;
	}

	private static void allNonDoublonFaceByCheckingNeigbhorsInTreeInner(Blocktree root, Blocktree node, List<Face> allFaces){
		switch (node.getState()) {
		case LEAF:
			for (Face face : node.getFaces()){
				if (shouldInsertFace(root, face)){
					allFaces.add(face);
				}
			}
			break;

		case FATHER : case GRAND_FATHER :
			for (Blocktree son : node.getSons()){
				allNonDoublonFaceByCheckingNeigbhorsInTreeInner(root, son, allFaces);
			}
		default:
			break;
		}
	}
	
	private static boolean shouldInsertFace(Blocktree root, Face face){
		DyadicBlock blockInFrontOfFace = face.getBlockInFrontOf();
		// find the smallest node containing the block in front of in the octree.
		Blocktree smallestNodeContainingBlockInFrontOfFace = root.smallestCellContaining(blockInFrontOfFace);		
		return (smallestNodeContainingBlockInFrontOfFace == null || 
				(smallestNodeContainingBlockInFrontOfFace.getState() != LEAF && 
				smallestNodeContainingBlockInFrontOfFace.getState() != DEAD_GROUND));
	}
	
	public static List<Face> allNonDoublonFaceFusionFast(Blocktree root){
		// first pass : check for doublon using the check reverse face belongs to hashset
		Set<Face> allFacesFast = allNonDoublonFaceByCheckingDoublonInHashSet(root);
		List<Face> allFaces = new ArrayList<Face>();
		for (Face face : allFacesFast){
			// second pass : check the status the state of the block in front of the face
			if (shouldInsertFace(root, face)){
				allFaces.add(face);
			}
		}
		return allFaces;
	}


}
