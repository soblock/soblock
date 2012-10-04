package org.wavecraft.gameobject.physics;

import java.util.EnumSet;

import org.wavecraft.gameobject.GameObjectMoving;
import org.wavecraft.gameobject.GameObjectMovingOriented;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardDown;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

//singleton
public class PhysicsFreeFlight extends Physics implements UiEventListener{
	
	private EnumSet<Direction> direction; // a combination of possible Direction values 
	private double scalarSpeedMult = 1;
	private double scalarSpeedDefault = 0.005;
	
	private static PhysicsFreeFlight instance;
	
	public static PhysicsFreeFlight getInstance(){
		if (instance == null){
			instance = new PhysicsFreeFlight();
		}
		instance.scalarSpeedMult =1; // each time we switch on this, reinit value
		return instance;
	}

	protected PhysicsFreeFlight(){
		direction = EnumSet.noneOf(Direction.class);
		UiEventMediator.addListener(this);
	}
	
	@Override
	public void move(GameObjectMoving movingObject, double dt) {
		
		movingObject.moove(dt);
		if (movingObject instanceof GameObjectMovingOriented){
			
			// get orientation and set speed to this orientation
			double theta = ((GameObjectMovingOriented) movingObject).getTheta();
			double phi = ((GameObjectMovingOriented) movingObject).getPhi();
			double ux = (Math.cos(theta) * Math.cos(phi));
			double uy = (Math.sin(theta) * Math.cos(phi));
			double uz = (Math.sin(phi));
			Coord3d speed = new Coord3d(0, 0, 0);
			double scalarSpeed = scalarSpeedMult * scalarSpeedDefault;
			if (direction.contains(Direction.FORWARD)){
				Coord3d speedToAdd = new Coord3d(scalarSpeed*ux, scalarSpeed*uy, scalarSpeed*uz);
				speed.add(speedToAdd);
			}
			if (direction.contains(Direction.BACKWARD)){
				Coord3d speedToAdd = new Coord3d(-scalarSpeed*ux,- scalarSpeed*uy,-scalarSpeed*uz);
				speed.add(speedToAdd);
			}
			if (direction.contains(Direction.LEFT)){
				Coord3d speedToAdd = new Coord3d(-scalarSpeed*uy, scalarSpeed*ux, 0);
				speed.add(speedToAdd);
			}
			if (direction.contains(Direction.RIGHT)){
				Coord3d speedToAdd = new Coord3d(scalarSpeed*uy, -scalarSpeed*ux, 0);
				speed.add(speedToAdd);
			}
			if (direction.contains(Direction.UP)){
				Coord3d speedToAdd = new Coord3d(0,0, scalarSpeed);
				speed.add(speedToAdd);
			}
			if (direction.contains(Direction.DOWN)){
				Coord3d speedToAdd = new Coord3d(0,0, -scalarSpeed);
				speed.add(speedToAdd);
			}
			
			movingObject.setSpeed(speed);
			direction.clear();
			scalarSpeedMult = 1;
		}
	}

	@Override
	public void handle(UiEvent event) {
		if (event instanceof UiEventKeyboardDown) {

			switch (((UiEventKeyboardDown) event).key) {
			case KEYBOARD_MOVE_FORWARD:
				direction.add(Direction.FORWARD);
				break;
			case KEYBOARD_MOVE_BACKWARD:
				direction.add(Direction.BACKWARD);
				break;
			case KEYBOARD_MOVE_STRAFELEFT:
				direction.add(Direction.LEFT);
				break;
			case KEYBOARD_MOVE_STRAFERIGHT:
				direction.add(Direction.RIGHT);
				break;
			case KEYBOARD_MOVE_UP:
				direction.add(Direction.UP);
				break;
			case KEYBOARD_MOVE_DOWN:
				direction.add(Direction.DOWN);
				break;
			case KEYBOARD_SPEED_UP1:
				scalarSpeedMult =4;
				break;
			case KEYBOARD_SPEED_UP2:
				scalarSpeedMult =16;
				break;
			case KEYBOARD_SPEED_UP3:
				scalarSpeedMult =64;
				break;
			case KEYBOARD_SPEED_UP4:
				scalarSpeedMult =256;
				break;
			default:
				break;
			}
		}

	}

}
