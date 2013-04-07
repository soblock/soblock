package org.wavecraft.geometry.blocktree;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.modif.ModifOctree;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionUtils;
import org.wavecraft.geometry.worldfunction.WorldFunction;


public class BlocktreeBuilderThreeDimFunModif implements BlocktreeBuilder{

	private WorldFunction wf;
	private BlocktreePriority priority;
	private ModifOctree modif;
	
	public ModifOctree getModif() {
		return modif;
	}

	public BlocktreeBuilderThreeDimFunModif(WorldFunction wf, BlocktreePriority priority, ModifOctree modif){
		this.wf = wf;
		this.priority = priority;
		this.modif = modif;
	}
	
	@Override
	public boolean isGround(DyadicBlock block) {
		ModifOctree cell=modif.smallestCellContainingBlock(block);
		double S, bmax,v;
		if (cell.getJ()>block.getJ() && cell.sons!=null){
			S=cell.sumAncestors;v=cell.value;bmax=0.;
		}
		else{
			S=cell.sumAncestors;bmax=cell.boundMax;v=cell.value;
		}
		double[] minmax =  ThreeDimFunctionUtils.minMaxValuesAtVertices(wf, block);
		double vMax=minmax[1]+S+v+bmax;
		return (vMax<0);
	}

	@Override
	public boolean isIntersectingSurface(DyadicBlock block) {
		ModifOctree cell=modif.smallestCellContainingBlock(block);
		double sumAncestors, bmax,bmin,v;
		if (cell.getJ()>block.getJ() && cell.sons!=null){
			sumAncestors=cell.sumAncestors;
			v=cell.value; 
			bmin=0.;
			bmax=0.;
		}
		else{
			sumAncestors=cell.sumAncestors;
			bmin=cell.boundMin;
			bmax=cell.boundMax;
			v=cell.value;
		}
		double[] minmax =  ThreeDimFunctionUtils.minMaxValuesAtVertices(wf, block);
		double vMin=minmax[0]+sumAncestors+v+bmin;
		double vMax=minmax[1]+sumAncestors+v+bmax;
		double JumpMax=modif.jumpMax(block);
		double Dphi = wf.uncertaintyBound(block);
		return !( vMin>Dphi // air
				|| vMax+JumpMax<-Dphi); // ground
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
	public Terran contentAt(DyadicBlock block) {
		ModifOctree smallestMod = modif.smallestNegativeCellContainingBlock(block);
		if (smallestMod != null && smallestMod.value<0){
			return smallestMod.content;
		}
		else {
			return wf.contentAt(block);
		}
	}

	@Override
	public double priority(Blocktree block) {
		return priority.priority(block);
	}

}
