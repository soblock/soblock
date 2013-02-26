package org.wavecraft.graphics.vbo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;

public class VBOBlockTreeGrandFather {


	private VBOWrapper vboWrapper;
	private Blocktree blocktree;

	public VBOBlockTreeGrandFather(Blocktree blockTree){
		// put all face in hashset
		this.blocktree = blockTree;
		HashSet<Face> allFaces = new HashSet<Face>();
		putAllFaceInHashetInner(blockTree, allFaces);
	}

	/**
	 * method of removing the doublon by checking if the reverse face is not in the hashset
	 * @param node
	 * @param allFaces
	 */
	public static void putAllFaceInHashetInner(Blocktree node, HashSet<Face> allFaces){
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

	private static List<Face> allNonDoublonFaceByCheckingNeigbhorsInTree(Blocktree root){
		List<Face> allFaces = new ArrayList<Face>();
		allNonDoublonFaceByCheckingNeigbhorsInTreeInner(root, root, allFaces);
		return allFaces;
	}

	private static void allNonDoublonFaceByCheckingNeigbhorsInTreeInner(Blocktree root, Blocktree node, List<Face> allFaces){
		switch (node.getState()) {
		case LEAF:
			for (Face face : node.getFaces()){
				DyadicBlock blockInFrontOfFace = face.getBlockInFrontOf();
				// find the smallest node containing the block in front of in the octree.
				Blocktree smallestNodeContainingBlockInFrontOfFace = root.smallestCellContaining(blockInFrontOfFace);
				if (smallestNodeContainingBlockInFrontOfFace != null){
					switch (smallestNodeContainingBlockInFrontOfFace.getState()) {
					case LEAF : case DEAD_GROUND :
						// do nothing, the face should not appear
						break;

					default:
						allFaces.add(face);
						break;
					}
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

}
