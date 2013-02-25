package org.wavecraft.graphics.vbo;

import java.util.HashSet;

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

}
