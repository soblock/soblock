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
				split(node);
			}
			break;

		case PATRIARCH:
			for (Blocktree son : node.getSons()){
				updateInner(son);
			}
			break;
		default:
			break;
		}
	}

	private void split(Blocktree greatFather){
		splitInner(greatFather, greatFather);
		greatFather.setState(PATRIARCH);
		for (Blocktree son : greatFather.getSons()){
			if (son.getState()==FATHER){ 
				son.setState(GRAND_FATHER);
			}
		}
	}

	private void splitInner(Blocktree node, Blocktree greatFather){
		if (node.getJ() >= greatFather.getJ() - BLOCK_LOG_SIZE ){

			node.setState(FATHER);
			if (!node.hasSons()){
				node.initSons();
			}
			for (Blocktree son : node.getSons()){
				if (builder.isIntersectingSurface(son)){
					son.setState(LEAF);
					splitInner(son, greatFather);
				} else {
					if (builder.isGround(son)){
						son.setState(DEAD_GROUND);
					} else {
						son.setState(DEAD_AIR);
					}
				}

			}
			int numberOfDeadAirSons = 0;
			int numberOfDeadGroundSons = 0;
			for (Blocktree son : node.getSons()){
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
			if (numberOfDeadAirSons==8){
				node.killSons();
				node.setState(DEAD_AIR);
			}
			if (numberOfDeadGroundSons==8){
				node.killSons();
				node.setState(DEAD_GROUND);
			}

		}

	}

}
