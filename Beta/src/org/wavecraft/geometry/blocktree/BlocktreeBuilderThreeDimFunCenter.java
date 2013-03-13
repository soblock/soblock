package org.wavecraft.geometry.blocktree;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionUtils;
import org.wavecraft.geometry.worldfunction.WorldFunction;


/**
 * thise class uses only the value at the center instead of the value at each vertex
 * to determine if the block is ground or surface or air
 * @author laurentsifre
 */
public class BlocktreeBuilderThreeDimFunCenter implements BlocktreeBuilder{

	private WorldFunction wf;
	private BlocktreePriority priority;
	
	public BlocktreeBuilderThreeDimFunCenter(WorldFunction wf, BlocktreePriority priority){
		this.wf = wf;
		this.priority = priority;
	}
	
	
	@Override
	public boolean isGround(DyadicBlock block) {
		return (wf.valueAt(block.center())<0);
	}

	@Override
	public boolean isIntersectingSurface(DyadicBlock block) {
		double[] minmax =  ThreeDimFunctionUtils.minMaxValuesAtVertices(wf, block);
		double vMin=minmax[0];
		double vMax=minmax[1];
		double Dphi = wf.uncertaintyBound(block);
		return !( vMin>Dphi|| vMax<-Dphi);
	}

	@Override
	public boolean shouldSplitGreatFatherToPatriarch(Blocktree block) {
		return priority(block)>1;
	}

	@Override
	public boolean shouldMergePatriarchIntoGreatFather(Blocktree block) {
		return priority(block)>1;
	}

	@Override
	public int contentAt(DyadicBlock block) {
		return wf.contentAt(block);
	}

	@Override
	public double priority(Blocktree block) {
		return priority.priority(block);
	}

}
