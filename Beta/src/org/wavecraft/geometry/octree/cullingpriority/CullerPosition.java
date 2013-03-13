package org.wavecraft.geometry.octree.cullingpriority;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeStateFatherCool;
import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.OctreeStateNotYetVisited;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

public class CullerPosition implements Culler, OctreePriorityFunction, UiEventListener{

	private Coord3d position;
	private double geoCullCoefficient = 4;
	private final static double minCullingCoefficient = 1;
	private final static double maxCullingCoefficient = 512;
	public CullerPosition(){
		position = new Coord3d(0, 0, 0);
		UiEventMediator.addListener(this);
	}

	public CullerPosition(Coord3d position){
		this.position = position;
		UiEventMediator.addListener(this);
	}

	@Override
	public boolean cull(Octree octree) {
		return (octree.getJ()==0 || octree.center().distance(position) > thres(octree.getJ()));
	}

	public double thres(int J){
		int J0 = 7;
		if (J<J0){
			return geoCullCoefficient*Math.pow(2, J);
		}
		else {
			return geoCullCoefficient*Math.pow(2, J0) + 2*(Math.pow(2, J) - Math.pow(2, J0));
		}
	}

	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){
			switch (((UiEventKeyboardPressed) e).key){
			case KEYBOARD_CULLING_DECREASE :
				geoCullCoefficient--;
				System.out.println(geoCullCoefficient);
				break;
			case KEYBOARD_CULLING_INCREASE:
				geoCullCoefficient++;
				System.out.println(geoCullCoefficient);
				break;
			default:
				break;
			}
			geoCullCoefficient = Math.max(minCullingCoefficient, geoCullCoefficient);
			geoCullCoefficient = Math.min(maxCullingCoefficient, geoCullCoefficient);
		}

	}

	@Override
	public double priority(DyadicBlock block) {

		if (block instanceof Octree){
			Octree node = (Octree) block;
			double p = 0;
			if (node.getState() instanceof OctreeStateLeaf ){ 
				if (node.getJ() == 0) return 100000000;
				//p = node.center().distance(position)/node.edgeLentgh();
				double ttj = node.edgeLentgh();
				synchronized (GameEngine.class) {
					p = node.distance(position)/(ttj);
				}
			}
			if (node.getState() instanceof OctreeStateNotYetVisited){
				//p = node.center().distance(position)/node.edgeLentgh() ;
				p = 0;
				//double ttj = node.edgeLentgh();
				//p = node.distance(position)/(ttj);
			}
			if (node.getState() instanceof OctreeStateFatherCool){
				//p = 0;
				synchronized (GameEngine.class) {
					p = node.edgeLentgh()/(node.distance(position)+1);
				}
				//p = node.center().distance(position)/node.edgeLentgh() ;
				//p =  node.edgeLentgh() / node.center().distance(position);
			}
			return p;
		}
		if (block instanceof Blocktree){
			Blocktree node = (Blocktree) block;
			double p = 0;
			switch (node.getState()) {
			case GRAND_FATHER:
				p = geoCullCoefficient*node.edgeLentgh()/(node.distance(position)+1);
				break;

			case PATRIARCH:
				p = 1/(geoCullCoefficient*node.edgeLentgh()/(node.distance(position)+1));

				break;
			default:
				break;
			}
			return p;
		}
		return 0;

	}

}
