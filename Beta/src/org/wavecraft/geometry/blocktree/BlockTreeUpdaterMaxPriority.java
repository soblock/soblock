package org.wavecraft.geometry.blocktree;


import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.blocktree.Blocktree.State;

public class BlocktreeUpdaterMaxPriority extends BlocktreeUpdaterSimple {

	public BlocktreeUpdaterMaxPriority(BlocktreeBuilder builder) {
		super(builder);
	}

	private Blocktree.State nextState = Blocktree.State.GRAND_FATHER;

	private Blocktree nodeToReplace;
	private Blocktree nodeReplacer;




	/**
	 * this method update the octree by 
	 * 1 - getting the node with maximum priority
	 * 2 - safely copying this node
	 * 3 - update the copy
	 * 4 - store the original and copy
	 * it is typically used to safely modify the octree
	 * in a separate thread
	 */
	public void updateANode(Blocktree root){
		Blocktree block;
		synchronized (GameEngine.class) {
			block =  getArgMaxPriorityPerState(root, nextState);
		}
		if (block != null){
			Blocktree blockSafe = block.cloneRecursively();
			updateInner(blockSafe);
			synchronized (GameEngine.class) {
				nodeToReplace = block;
				nodeReplacer = blockSafe;
			}
		}
		if (nextState==State.GRAND_FATHER){
			nextState = State.PATRIARCH;
		}
		else {
			nextState = State.GRAND_FATHER;
		}
	}

	private class BlocktreeAndValue{
		public double value;
		public Blocktree node;
		public BlocktreeAndValue(){
			this.value = 0;
			this.node = null;
		}
	}

	public Blocktree getArgMaxPriorityPerState(Blocktree root, Blocktree.State state){
		BlocktreeAndValue currentMaxAndArgMax = new BlocktreeAndValue();
		getArgMaxPriorityPerStateInner(root, currentMaxAndArgMax, state);
		//System.out.println(" argmax "+currentMaxAndArgMax.node + " priority " + currentMaxAndArgMax.value);
		if (currentMaxAndArgMax.node!=null){
		switch (state) {
		case GRAND_FATHER:
			if (!builder.shouldSplitGreatFatherToPatriarch(currentMaxAndArgMax.node)) {
				return null;
			}
			break;

		case PATRIARCH :
			if (!builder.shouldMergePatriarchIntoGreatFather(currentMaxAndArgMax.node)){
				return null;
			}
			break;
		default:
			break;
		}
		}
		return currentMaxAndArgMax.node;
	}

	private void getArgMaxPriorityPerStateInner(Blocktree node, BlocktreeAndValue currentMaxAndArgMax, Blocktree.State state){
		if (node.getState() == State.PATRIARCH || node.getState() == State.GRAND_FATHER){
			if (node.getState()==state){
				double p = builder.priority(node);
				if (p>currentMaxAndArgMax.value){
					currentMaxAndArgMax.value = p;
					currentMaxAndArgMax.node = node; 
				}
			}
			for (Blocktree son : node.getSons()){
				getArgMaxPriorityPerStateInner(son, currentMaxAndArgMax, state);
			}

		}

	}


}
