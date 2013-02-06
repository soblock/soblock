package org.wavecraft.geometry.blocktree;

import org.wavecraft.geometry.DyadicBlock;

public interface BlocktreeBuilder {

	public boolean isGround(DyadicBlock block);
	public boolean isIntersectingSurface(DyadicBlock block);
	public boolean shouldSplitGreatFatherToPatriarch(DyadicBlock block);
	public boolean shouldMergePatriarchIntoGreatFather(DyadicBlock block);

}
