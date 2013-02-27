package org.wavecraft.graphics.vbo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.junit.Test;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.BlocktreeBuilder;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderAdapter;
import org.wavecraft.geometry.blocktree.BlocktreeUpdaterSimple;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.octree.builder.OctreeBuilderBuilder;

import static org.junit.Assert.assertTrue;
import static org.wavecraft.geometry.blocktree.Blocktree.State.*;


public class VBOBlockTreeGrandFatherTest {

	public VBOBlockTreeGrandFatherTest(){};
	
	
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
		Set<Face> allFaces = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingDoublonInHashSet(blockTree);
		assertTrue(allFaces.size() == 4*6); 
		
		assertTrue(allFaces.contains(new Face(0, 0, 0, 0, -1)));
		assertTrue(allFaces.contains(new Face(0, 0, 0, 0, -2)));
		assertTrue(allFaces.contains(new Face(0, 0, 0, 0, -3)));
		assertTrue(!allFaces.contains(new Face(0, 0, 0, 0, 1)));
		assertTrue(!allFaces.contains(new Face(0, 0, 0, 0, 2)));
		assertTrue(!allFaces.contains(new Face(0, 0, 0, 0, 3)));
		assertTrue(allFaces.contains(new Face(1, 1, 2, 0, 3)));
		assertTrue(allFaces.contains(new Face(1, 2, 1, 0, 2)));
		assertTrue(allFaces.contains(new Face(2, 1, 1, 0, 1)));
		
		List<Face> allFaceCheckNeighbors = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingNeigbhorsInTree(blockTree);
		assertTrue(allFaceCheckNeighbors.size() == 24);
		
		blockTree.getSons()[0].getSons()[0].setState(DEAD_AIR); // should stay the same 24
		allFaceCheckNeighbors = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingNeigbhorsInTree(blockTree);
		allFaces = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingDoublonInHashSet(blockTree);
		assertTrue(allFaceCheckNeighbors.size() == 24);
		assertTrue(allFaces.size() == 24);
		
		blockTree.getSons()[0].getSons()[1].setState(DEAD_AIR); // should go down to 22
		allFaceCheckNeighbors = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingNeigbhorsInTree(blockTree);
		allFaces = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingDoublonInHashSet(blockTree);
		assertTrue(allFaceCheckNeighbors.size() == 22);
		assertTrue(allFaces.size() == 22);
		
		// compare run time
		int nRun = 100000;
		double t1 = System.currentTimeMillis();
		for (int i = 0; i<nRun ; i++){
			allFaces = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingDoublonInHashSet(blockTree);
		}
		double t2 = System.currentTimeMillis();
		for (int i = 0; i<nRun ; i++){
			allFaceCheckNeighbors = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingNeigbhorsInTree(blockTree);
		}
		double t3 = System.currentTimeMillis();
		
		System.out.println("hash set method "+(t2-t1));
		System.out.println("check neighbor method "+(t3-t2));
	}
	
	@Test
	public void testRunTimeLargeScale(){
		Blocktree blocktree = new Blocktree(0, 0, 0, 5);
		BlocktreeBuilder builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandNoculling(12.5));
		

		blocktree.setState(State.GRAND_FATHER);
		BlocktreeUpdaterSimple blockTreeUpdaterSimple = new BlocktreeUpdaterSimple(builder);
		blockTreeUpdaterSimple.init(blocktree);
		

		
		// compare run time
		int nRun = 10000;
		double t1 = System.currentTimeMillis();
		Set<Face> allFaces = null;
		List<Face> allFaceCheckNeighbors = null;
		for (int i = 0; i<nRun ; i++){
			 allFaces = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingDoublonInHashSet(blocktree);
		}
		double t2 = System.currentTimeMillis();
		for (int i = 0; i<nRun ; i++){
			allFaceCheckNeighbors = VBOBlockTreeGrandFather.allNonDoublonFaceByCheckingNeigbhorsInTree(blocktree);
		}
		double t3 = System.currentTimeMillis();
		
		System.out.println("large scale hash set method "+(t2-t1));
		System.out.println("large scale check neighbor method "+(t3-t2));
		System.out.println("number of face for hash set method "+ allFaces.size());
		System.out.println("number of face for neighbor "+ allFaceCheckNeighbors.size());
		// 8*8*2 + 8*4 = 160
		// 8*8 + 8*4 = 96 : the first method is faster but computes more face...
		
		
	}

}
