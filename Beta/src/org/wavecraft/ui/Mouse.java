package org.wavecraft.ui;

import javax.vecmath.Point2d;

import org.wavecraft.geometry.Coord2d;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMouseClicked;
import org.wavecraft.ui.events.UiEventMouseMoved;


// singleton 

public class Mouse implements UiEventListener{
	private double sensitivity=0.01;
	private Point2d lastMove;
	 

	private static Mouse instance;
	public static Mouse getInstance(){
		if (instance == null){
			instance = new Mouse();
		}
		return instance; 
	}
	private Mouse(){
		lastMove = new Point2d(0,0);
		org.lwjgl.input.Mouse.setGrabbed(false);
		UiEventMediator.addListener(this);
	}
	private void getUpdate(){
		lastMove.x = sensitivity*org.lwjgl.input.Mouse.getDX() ; 
		lastMove.y = sensitivity*org.lwjgl.input.Mouse.getDY();	
	}

	public void getMouseEvent(){
		getUpdate();
		// move event
		if (lastMove.x!= 0 && lastMove.y!=0){
			UiEvent eventMoved = new UiEventMouseMoved(lastMove);
			UiEventMediator.addEvent(eventMoved);
		}

		// click events
		while (org.lwjgl.input.Mouse.next()){
			int buttonId = org.lwjgl.input.Mouse.getEventButton();
			if (buttonId > -1){
				boolean buttonState = org.lwjgl.input.Mouse.getEventButtonState();
				int x = org.lwjgl.input.Mouse.getEventX();
				int y = org.lwjgl.input.Mouse.getEventY();

				UiEventMouseClicked eventClicked = new UiEventMouseClicked(new Coord2d(x, y), buttonId, buttonState);
				UiEventMediator.addEvent(eventClicked);
			}
		}

	}
	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){
			KeyboardBinding keyBind = ((UiEventKeyboardPressed) e).key;
			if (keyBind == KeyboardBinding.KEYBOARD_GRAB_MOUSE){
				org.lwjgl.input.Mouse.setGrabbed(!org.lwjgl.input.Mouse.isGrabbed());
			}
		}

	}


}
