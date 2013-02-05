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
		switch (root.getState()) {
		case GRAND_FATHER:
			if (builder.shouldSplitGreatFatherToPatriarch(node)){
				split(node);
			}
			break;

		default:
			break;
		}
	}

	private void split(Blocktree greatFather){

	}

	private void splitInner(Blocktree node, Blocktree greatFather){
		if (node.getJ() > greatFather.getJ() - BLOCK_LOG_SIZE ){
			node.setState(FATHER);
			node.initSons();
			for (Blocktree son : node.getSons()){
				splitInner(son, greatFather);
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
