package org.wavecraft.geometry.FluidTree;


import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;

public class FluidTreeStateFixed extends FluidTreeState {
	

	private static FluidTreeStateFixed state = null;
	public static FluidTreeStateFixed getInstance(){
		if (state==null){
			state = new FluidTreeStateFixed();
		}
		return state;
	}
	


	@Override
	public float internalJob(FluidTree fluidtree, OctreeBuilder builder) {
		// TODO check culling 
		if (builder.cull(fluidtree)){
			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.LEAFY);
			OctreeEventMediator.addEvent(event);
			return 1;
		}
		
		return 0;
	}

}
