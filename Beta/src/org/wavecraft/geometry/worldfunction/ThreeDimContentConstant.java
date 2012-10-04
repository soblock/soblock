package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimContentConstant implements ThreeDimContent {

	@Override
	public int contentAt(DyadicBlock block) {
		//return 8;
		return (int) ((block.x*block.edgeLentgh()/128)%7);
		//return (int) (1 + block.center().distance(new Coord3d(128,128,128))/100 %6);
	}
	
}
