package org.wavecraft.geometry.octree;

import org.wavecraft.geometry.octree.builder.OctreeBuilder;

//singleton
public class OctreeStateDead extends OctreeState{

	
	private static OctreeStateDead state = null;
	public static OctreeStateDead getInstance(){
		if (state==null){
			state = new OctreeStateDead();
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
