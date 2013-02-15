package org.wavecraft.geometry.blocktree;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.builder.OctreeBuilder;

public class BlocktreeBuilderAdapter implements BlocktreeBuilder {

	private OctreeBuilder builder;
	
	
	public BlocktreeBuilderAdapter(OctreeBuilder builder){
		this.builder = builder;
	}

	@Override
	public boolean isGround(DyadicBlock block) {
		// TODO :
		return builder.isGround(new Octree(block, null));
	}

	@Override
	public boolean isIntersectingSurface(DyadicBlock block) {
		return !builder.isOutsideDomainOfInterest(new Octree(block, null));
	}

	@Override
	public boolean shouldSplitGreatFatherToPatriarch(DyadicBlock block) {
		return !builder.cull(new Octree(block, null));
	}

	@Override
	public boolean shouldMergePatriarchIntoGreatFather(DyadicBlock block) {
		return builder.cull(new Octree(block, null));
	}

	@Override
	public int contentAt(DyadicBlock block) {
		return builder.contentAt(block);
	}

	@Override
	public double priority(DyadicBlock block) {
		return builder.priority(block);
	}
	


}
