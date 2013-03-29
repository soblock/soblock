package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Terran;

public class ThreeDimContentConstant implements ThreeDimContent {

	@Override
	public Terran contentAt(DyadicBlock block) {
		return Terran.NAT_SAND;
	}
	
}
