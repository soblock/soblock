package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Terran;

public interface ThreeDimContent{
	public Terran contentAt(DyadicBlock block);
}
