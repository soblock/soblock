package org.wavecraft.geometry.FluidTree;

import org.wavecraft.geometry.octree.builder.OctreeBuilder;


public abstract class FluidTreeState {

	public abstract float internalJob(FluidTree fluidtree, OctreeBuilder builder);
	// the reurned value represent the 'computational cost' of the internal job.

	public static void update(FluidTree fluidtree, OctreeBuilder builder){
		fluidtree.getState().internalJob(fluidtree, builder);
		if (fluidtree.hasSons()){
			for (int i = 0; i<8 ; i++){
				update(fluidtree.sons[i], builder);
			}
		}
	}

}
