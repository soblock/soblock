package org.wavecraft.geometry.octree;



import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;

// the worried father knows some of his children might be dead
// if at least one of them is alive, he will calm down
// if not, he will kill himself
// singleton
public class OctreeStateFatherWorried extends OctreeState{

	private static OctreeStateFatherWorried state = null;
	public static OctreeStateFatherWorried getInstance(){
		if (state==null){
			state = new OctreeStateFatherWorried();
		}
		return state;
	}
	//	@Override
	//	public void update(Octree octree, OctreeBuilder builder) {
	//		
	//	}
	@Override
	public float internalJob(Octree octree, OctreeBuilder builder) {
		// TODO Auto-generated method stub
		int deadSons = 0;
		int groundSons = 0;
		Octree[] sons = octree.getSons();
		for (int offset = 0; offset < 8;  offset++){
			if (sons[offset].getState() instanceof OctreeStateDead ){
				deadSons++;
			}
			if (sons[offset].getState() instanceof OctreeStateGround){
				groundSons++;
			}
		}

		if (deadSons == 8){
			// commit suicide
			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.KILL);
			OctreeEventMediator.addEvent(event);
		}
		if (groundSons == 8){
			// commit suicide
			OctreeEvent event = new OctreeEvent(octree, OctreeEventKindof.KILLGROUND);
			OctreeEventMediator.addEvent(event);
		}
		//else{
		if (deadSons <8 && groundSons< 8) {
			// calm down
			octree.setState(OctreeStateFatherCool.getInstance());
		}
		return 0;
	}


}
