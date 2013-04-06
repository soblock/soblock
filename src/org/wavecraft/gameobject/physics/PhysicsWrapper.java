package org.wavecraft.gameobject.physics;

import org.wavecraft.gameobject.GameObjectMoving;
import org.wavecraft.ui.KeyboardBinding;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

public class PhysicsWrapper extends Physics implements UiEventListener{

	private Physics physics = PhysicsFreeFlightIntersect.getInstance();

	public PhysicsWrapper(){
		UiEventMediator.getUiEventMediator().addListener(this);
		PhysicsFreeFlight.getInstance();
		PhysicsWalkIntersect.getInstance();
	}
	
	@Override
	public void move(GameObjectMoving movingObject, double dt) {
		physics.move(movingObject, dt);
	}
	
	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){
			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_SWITCH_PHYSICS){
				switchPhys();
			}

		}
	}
	
	public void switchPhys(){
	if (physics instanceof PhysicsFreeFlight){
		physics = PhysicsWalkIntersect.getInstance();
		System.out.println("WALK INTERSECT");
	}
	else{
		if (physics instanceof PhysicsWalkIntersect){
			physics = PhysicsFreeFlightIntersect.getInstance();
			System.out.println("FREE FLIGHT MODE");
		}
	}
	}

}
