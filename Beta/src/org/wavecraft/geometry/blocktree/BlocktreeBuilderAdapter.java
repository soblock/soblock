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
	public boolean shouldSplitGreatFatherToPatriarch(Blocktree block) {
		return (priority(block)>1);	
	}

	@Override
	public boolean shouldMergePatriarchIntoGreatFather(Blocktree block) {
		return priority(block)>1;
	}

	@Override
	public int contentAt(DyadicBlock block) {
		return builder.contentAt(block);
	}

	@Override
	public double priority(Blocktree block) {
		if (block.getJ()<=Blocktree.BLOCK_LOG_SIZE +1) {return 0;}
		return builder.priority(block);
	}
	


}
