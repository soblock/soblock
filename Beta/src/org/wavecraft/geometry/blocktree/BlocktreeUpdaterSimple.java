package org.wavecraft.geometry.blocktree;

import static org.wavecraft.geometry.blocktree.Blocktree.State.*;

public class BlocktreeUpdaterSimple implements BlocktreeUpdater {

	private BlocktreeBuilder builder;
	private Blocktree root;
	private int BLOCK_LOG_SIZE = 3;

	public BlocktreeUpdaterSimple(Blocktree root, BlocktreeBuilder builder){
		this.root = root;
		this.builder = builder;
	}

	@Override
	public void update() {
		updateInner(root);
	}

	private void updateInner(Blocktree node){
		switch (node.getState()) {
		case GRAND_FATHER:
			if (node.getJ()>BLOCK_LOG_SIZE && builder.shouldSplitGreatFatherToPatriarch(node)){
				splitAllLeaf(node);
				clean(node);
			}
			break;

		case PATRIARCH:
			for (Blocktree son : node.getSons()){
				updateInner(son);
			}
			if (builder.shouldMergePatriarchIntoGreatFather(node)){
				mergeAllLeaf(node);
			}
			break;
		default:
			break;
		}
	}

	public void init(){
		root.setState(LEAF);
		initInner(root);
		root.setState(GRAND_FATHER);
	}

	public void initInner(Blocktree node){
		if (node.getJ()>= root.getJ() - BLOCK_LOG_SIZE ){
			splitLeaf(node);
			if (node.getState()==FATHER){
				for (Blocktree son : node.getSons()){
					initInner(son);
				}
			}
		}
	}

	private void clean(Blocktree node){
		int numberOfDeadAirSons = 0;
		int numberOfDeadGroundSons = 0;
		if (node.hasSons()){
			for (Blocktree son : node.getSons()){
				clean(son);
				switch (son.getState()) {
				case DEAD_AIR:
					numberOfDeadAirSons++;
					break;

				case DEAD_GROUND:
					numberOfDeadGroundSons++;
					break;

				default :
					break;
				}
			}
		}
		if (numberOfDeadAirSons==8){
			node.killSons();
			node.setState(DEAD_AIR);
		}
		if (numberOfDeadGroundSons==8){
			node.killSons();
			node.setState(DEAD_GROUND);
		}

	}

	private void splitAllLeaf(Blocktree greatFather){
		splitAllLeafInner(greatFather, greatFather);
		greatFather.setState(PATRIARCH);
		for (Blocktree son : greatFather.getSons()){
			if (son.getState()==FATHER){
				son.setState(GRAND_FATHER);
			}
		}
	}

	private void splitAllLeafInner(Blocktree node, Blocktree greatFather){
		if (node.getJ()>= greatFather.getJ() - BLOCK_LOG_SIZE ){
			if (node.hasSons()){
				for (Blocktree son : node.getSons()){
					splitAllLeafInner(son, greatFather);
				}
			}
		}
		else {
			splitLeaf(node);
		}
	}

	private void splitLeaf(Blocktree leaf){
		if (leaf.getState()==LEAF){
			leaf.initSons();
			for (Blocktree son : leaf.getSons()){
				if (builder.isIntersectingSurface(son)){
					son.setState(LEAF);
				} else {
					if (builder.isGround(son)){
						son.setState(DEAD_GROUND);
					} else {
						son.setState(DEAD_AIR);
					}
				}
			}
			leaf.setState(FATHER);
		}
	}

	private void mergeAllLeaf(Blocktree patriarch){
		mergeAllLeafInner(patriarch, patriarch);
		patriarch.setState(GRAND_FATHER);
		for (Blocktree son : patriarch.getSons()){
			if (son.getState()==GRAND_FATHER){
				son.setState(FATHER);
			}
		}
	}

	private void mergeAllLeafInner(Blocktree patriarch, Blocktree node){
		if (node.getJ()>= patriarch.getJ() - BLOCK_LOG_SIZE ){
			if (node.hasSons()){
				for (Blocktree son : node.getSons()) {
					mergeAllLeafInner(patriarch, son);
				}
			}
		}
		else {
			mergeLeaf(node);
		}
	}

	private void mergeLeaf(Blocktree father){
		if (father.getState()==FATHER){
			father.killSons();
			father.setState(LEAF);
		}
	}





}
