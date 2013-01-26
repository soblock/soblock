package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimContentConstant implements ThreeDimContent {

	@Override
	public int contentAt(DyadicBlock block) {
		return (int) ((block.x*block.edgeLentgh()/128)%7);
	}
	
}
