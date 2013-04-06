package org.wavecraft.geometry;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class FaceTest {

	@Test
	public void testInFrontOfVertice() {
		Face face = new Face(1, 2, 3, 4, 1);
		DyadicBlock[] blocks = face.inFrontOfVertice(new Coord3i(16, 32, 48)); 
		List<DyadicBlock> blockList = Arrays.asList(blocks);
		HashSet<DyadicBlock> setOfBlock = new HashSet<DyadicBlock>(blockList);
		System.out.println(setOfBlock);
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2-1, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2-1, 3-1, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2, 3-1, 4)));
	}

	@Test
	public void testInFrontOfVertice2() {
		Face face = new Face(1, 2, 3, 4, -1);
		DyadicBlock[] blocks = face.inFrontOfVertice(new Coord3i(16, 32, 48)); 
		List<DyadicBlock> blockList = Arrays.asList(blocks);
		HashSet<DyadicBlock> setOfBlock = new HashSet<DyadicBlock>(blockList);
		System.out.println(setOfBlock);
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2-1, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2-1, 3-1, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2, 3-1, 4)));
	}
	
	@Test
	public void testInFrontOfVertice3() {
		Face face = new Face(1, 2, 3, 4, 2);
		DyadicBlock[] blocks = face.inFrontOfVertice(new Coord3i(16, 32, 48)); 
		List<DyadicBlock> blockList = Arrays.asList(blocks);
		HashSet<DyadicBlock> setOfBlock = new HashSet<DyadicBlock>(blockList);
		System.out.println(setOfBlock);
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2, 3-1, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2, 3-1, 4)));
	}
	
	@Test
	public void testInFrontOfVertice4() {
		Face face = new Face(1, 2, 3, 4, -2);
		DyadicBlock[] blocks = face.inFrontOfVertice(new Coord3i(16, 32, 48)); 
		List<DyadicBlock> blockList = Arrays.asList(blocks);
		HashSet<DyadicBlock> setOfBlock = new HashSet<DyadicBlock>(blockList);
		System.out.println(setOfBlock);
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2-1, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2-1, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2-1, 3-1, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2-1, 3-1, 4)));
	}

	@Test
	public void testInFrontOfVertice5() {
		Face face = new Face(1, 2, 3, 4, 3);
		DyadicBlock[] blocks = face.inFrontOfVertice(new Coord3i(16, 32, 48)); 
		List<DyadicBlock> blockList = Arrays.asList(blocks);
		HashSet<DyadicBlock> setOfBlock = new HashSet<DyadicBlock>(blockList);
		System.out.println(setOfBlock);
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2-1, 3, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2-1, 3, 4)));
	}
	

	@Test
	public void testInFrontOfVertice6() {
		Face face = new Face(1, 2, 3, 4, -3);
		DyadicBlock[] blocks = face.inFrontOfVertice(new Coord3i(16, 32, 48)); 
		List<DyadicBlock> blockList = Arrays.asList(blocks);
		HashSet<DyadicBlock> setOfBlock = new HashSet<DyadicBlock>(blockList);
		System.out.println(setOfBlock);
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2, 3-1, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2, 3-1, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1, 2-1, 3-1, 4)));
		assertTrue(setOfBlock.contains(new DyadicBlock(1-1, 2-1, 3-1, 4)));
	}

}
