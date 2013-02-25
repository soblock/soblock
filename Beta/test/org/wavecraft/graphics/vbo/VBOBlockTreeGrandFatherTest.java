package org.wavecraft.graphics.vbo;

import java.util.HashSet;


import org.junit.Test;

import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;

import static org.junit.Assert.assertTrue;
import static org.wavecraft.geometry.blocktree.Blocktree.State.*;


public class VBOBlockTreeGrandFatherTest {

	public VBOBlockTreeGrandFatherTest(){};
	
	@Test
	public void putAllFacesTest(){
		Blocktree blockTree = new Blocktree(0, 0, 0, 2);
		blockTree.setState(GRAND_FATHER);
		blockTree.initSons();
		for (Blocktree son : blockTree.getSons()){
			son.setState(DEAD_GROUND);
		}
		Blocktree son0 = blockTree.getSons()[0];
		son0.setState(FATHER);
		son0.initSons();
		for (Blocktree son : son0.getSons()){
			son.setState(LEAF);
		}

		//VBOBlockTreeGrandFather vboBTGF = new VBOBlockTreeGrandFather(blockTree);
		HashSet<Face> allFaces = new HashSet<Face>();
		VBOBlockTreeGrandFather.putAllFaceInHashetInner(blockTree, allFaces);
		System.out.println(allFaces.size());
		System.out.println(son0);
		assertTrue(allFaces.size() == 4*6); 
		for (Face face: allFaces){
			System.out.println(face);
		}
		assertTrue(allFaces.contains(new Face(0, 0, 0, 0, -1)));
		assertTrue(allFaces.contains(new Face(0, 0, 0, 0, -2)));
		assertTrue(allFaces.contains(new Face(0, 0, 0, 0, -3)));
		assertTrue(!allFaces.contains(new Face(0, 0, 0, 0, 1)));
		assertTrue(!allFaces.contains(new Face(0, 0, 0, 0, 2)));
		assertTrue(!allFaces.contains(new Face(0, 0, 0, 0, 3)));
		assertTrue(allFaces.contains(new Face(1, 1, 2, 0, 3)));
		assertTrue(allFaces.contains(new Face(1, 2, 1, 0, 2)));
		assertTrue(allFaces.contains(new Face(2, 1, 1, 0, 1)));
		
		
			
	}

}
