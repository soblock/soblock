package org.wavecraft.geometry.blocktree;

import static org.junit.Assert.*;
import static org.wavecraft.geometry.blocktree.Blocktree.State.DEAD_GROUND;
import static org.wavecraft.geometry.blocktree.Blocktree.State.FATHER;
import static org.wavecraft.geometry.blocktree.Blocktree.State.GRAND_FATHER;
import static org.wavecraft.geometry.blocktree.Blocktree.State.LEAF;

import java.util.List;

import org.junit.Test;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.octree.builder.OctreeBuilderBuilder;

public class BlocktreeTest {

	@Test
	public void test() {
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
		DyadicBlock query = new DyadicBlock(0, 0, 0, 0);
		Blocktree retrieved = blockTree.smallestCellContaining(query);
		assertTrue(query.equals(retrieved));
		query = new DyadicBlock(8, 0, 0, 0);
		retrieved = blockTree.smallestCellContaining(query);
		assertTrue(retrieved == null);
		
	}
	
	@Test
	public void testContains(){
		Blocktree big = new Blocktree(0, 0, 0, 1);
		Blocktree small = new Blocktree(-1, 0, 0, 0);
		
		assertFalse(big.contains(small));
		
		Blocktree blocktree = new Blocktree(0, 0, 0, 5);
		BlocktreeBuilder builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandNoculling(16.5));
		

		blocktree.setState(State.GRAND_FATHER);
		BlocktreeUpdaterSimple blockTreeUpdaterSimple = new BlocktreeUpdaterSimple(builder);
		blockTreeUpdaterSimple.init(blocktree);
		

		DyadicBlock block = new DyadicBlock(0, 0, 3, 2);
		assertTrue(blocktree.smallestCellContaining(block) != null);
	}
	
	@Test
	public void testListOfGreatChildren(){

		Blocktree blocktree = new Blocktree(0, 0, 0, 5);
		BlocktreeBuilder builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandNoculling(16.5));
		

		blocktree.setState(State.GRAND_FATHER);
		BlocktreeUpdaterSimple blockTreeUpdaterSimple = new BlocktreeUpdaterSimple(builder);
		blockTreeUpdaterSimple.init(blocktree);

		
		List<Blocktree> listOfGreatChildren = blocktree.listOfGreatChildren();
		System.out.println(listOfGreatChildren);
		assertTrue(blocktree.getState() == GRAND_FATHER);
	}
	

}
