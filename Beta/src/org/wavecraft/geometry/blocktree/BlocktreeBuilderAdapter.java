package org.wavecraft.geometry.blocktree;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.builder.OctreeBuilder;

public class BlocktreeBuilderAdapter implements BlocktreeBuilder {

	private OctreeBuilder builder;

	@Override
	public boolean isAir(DyadicBlock block) {
		// TODO :
		return false;
	}

	@Override
	public boolean isIntersectingSurface(DyadicBlock block) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldSplitGreatFatherToPatriarch(DyadicBlock block) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldMergePatriarchIntoGreatFather(DyadicBlock block) {
		// TODO Auto-generated method stub
		return false;
	}
	


}
