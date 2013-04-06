package org.wavecraft.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

public class DyadicBlockTest {

	@Test
	public void testContains() {
		DyadicBlock bigBlock = new DyadicBlock(1, 2, 3, 2);
		int ttj = (int) Math.pow(2, bigBlock.getJ());
		for (int i=0; i<ttj; i++)
			for (int j=0; j<ttj; j++)
				for (int k=0; k<ttj; k ++){
					DyadicBlock smallBlock = new DyadicBlock(bigBlock.x*ttj + i, bigBlock.y*ttj + j, bigBlock.z*ttj + k, 0);
					assertTrue(bigBlock.contains(smallBlock));
				}
		assertFalse(bigBlock.contains(new DyadicBlock(0, 0, 0, 0)));
	}
	
	@Test
	public void testSonsContaining() {
		DyadicBlock bigBlock = new DyadicBlock(1, 1, 1, 1);
		DyadicBlock smallBlockNotIn = new DyadicBlock(0, 0, 0, 0);
		assertTrue(bigBlock.findSonContaining(smallBlockNotIn) == -1);
		DyadicBlock smallBlockIn = new DyadicBlock(2, 2, 2, 0);
		System.out.println(bigBlock.findSonContaining(smallBlockIn) == 0);
	}

}
