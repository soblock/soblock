package org.wavecraft.client;


import org.wavecraft.geometry.octree.Octree;

public class WaveCraftBlockTreeSP extends WaveCraftSP{

	@Override protected void init(){
		Octree.JMAX=10;
		super.init();
		
	}
	

}
