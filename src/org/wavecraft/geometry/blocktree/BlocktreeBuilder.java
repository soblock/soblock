package org.wavecraft.geometry.blocktree;

import org.wavecraft.geometry.DyadicBlock;

public interface BlocktreeBuilder extends BlocktreePriority{

	public boolean isGround(DyadicBlock block);
	public boolean isIntersectingSurface(DyadicBlock block);
	public boolean shouldSplitGreatFatherToPatriarch(Blocktree block);
	public boolean shouldMergePatriarchIntoGreatFather(Blocktree block);
	public Terran contentAt(DyadicBlock block);


}
