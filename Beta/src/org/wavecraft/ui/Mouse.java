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

public class Mouse {
	private double sensitivity=0.01;
	private Point2d lastMove;

	public enum State{
		ACTIVE,
		UNACTIVE
	}
	private State state;



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
	}
	private void getUpdate(){
		if (state == State.ACTIVE){
			lastMove.x = sensitivity*org.lwjgl.input.Mouse.getDX() ; 
			lastMove.y = sensitivity*org.lwjgl.input.Mouse.getDY();
		}
	}

	public void getMouseEvent(){

		if (state == State.ACTIVE){
			getUpdate();
			// move event
			if (lastMove.x!= 0 && lastMove.y!=0){
				UiEvent eventMoved = new UiEventMouseMoved(lastMove);
				UiEventMediator.getUiEventMediator().addEvent(eventMoved);
			}

			// click events
			while (org.lwjgl.input.Mouse.next()){
				int buttonId = org.lwjgl.input.Mouse.getEventButton();
				if (buttonId > -1){
					boolean buttonState = org.lwjgl.input.Mouse.getEventButtonState();
					int x = org.lwjgl.input.Mouse.getEventX();
					int y = org.lwjgl.input.Mouse.getEventY();

					UiEventMouseClicked eventClicked = new UiEventMouseClicked(new Coord2d(x, y), buttonId, buttonState);
					UiEventMediator.getUiEventMediator().addEvent(eventClicked);
				}
			}
		}
	}


	public void setState(State state) {
		this.state = state;
	}

}
