package org.wavecraft.gameobject;

import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMouseMoved;

public class Player extends GameObjectMovingOriented implements UiEventListener{
	
	public Player(){
		super();
		this.boundingBox = new BoundingBox(new Coord3d(-0.5, -0.5, -2), new Coord3d(0.5,0.5 , 0.5));
		//this.boundingBox = new BoundingBox(new Coord3d(-10, -0.5, -10), new Coord3d(0.5,0.5 , 0.5));
	}

	
	@Override
	public void handle(UiEvent event) {
		if (event instanceof UiEventMouseMoved){
			
			double theta = this.getTheta();
			double phi = this.getPhi();
			theta -= ((UiEventMouseMoved) event).move.x;
			phi += ((UiEventMouseMoved) event).move.y;
			if (phi > Math.PI/2) {
				phi = Math.PI/2 - 0.01;
			}
			if (phi < - Math.PI/2){
				phi = - Math.PI/2 + 0.01;
			}
			this.setTheta(theta);
			this.setPhi(phi);
		}
		
		
	}
	
}
