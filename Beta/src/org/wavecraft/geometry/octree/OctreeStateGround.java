package org.wavecraft.geometry.octree;

import org.wavecraft.geometry.octree.builder.OctreeBuilder;

//singleton
public class OctreeStateGround extends OctreeState{

	
	private static OctreeStateGround state = null;
	public static OctreeStateGround getInstance(){
		if (state==null){
			state = new OctreeStateGround();
		}
		return state;
	}
	


	@Override
	public float internalJob(Octree octree, OctreeBuilder builder) {
		// TODO Auto-generated method stub
		// im dead : do nothing
		return 0;
	}


	

	

}
