package org.wavecraft.geometry.blocktree;

import static org.junit.Assert.*;
import static org.wavecraft.geometry.blocktree.Blocktree.State.DEAD_GROUND;
import static org.wavecraft.geometry.blocktree.Blocktree.State.FATHER;
import static org.wavecraft.geometry.blocktree.Blocktree.State.GRAND_FATHER;
import static org.wavecraft.geometry.blocktree.Blocktree.State.LEAF;

import org.junit.Test;
import org.wavecraft.geometry.DyadicBlock;

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

}
