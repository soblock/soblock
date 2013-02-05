package org.wavecraft.client;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.octree.Octree;

public class WaveCraftBlockTreeSP extends WaveCraftSP{

	@Override protected void init(){
		//Octree.JMAX=0;
		super.init();
		GameEngine.setBlocktree(new Blocktree(0, 0, 0, 0));
	}
	

}
