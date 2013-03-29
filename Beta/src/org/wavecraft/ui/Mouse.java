package org.wavecraft.ui;

import javax.vecmath.Point2d;

import org.wavecraft.geometry.Coord2d;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMenu;
import org.wavecraft.ui.events.UiEventMouseClicked;
import org.wavecraft.ui.events.UiEventMouseMoved;
import org.wavecraft.ui.menu.twl.MainMenu;


// singleton 

public class Mouse implements UiEventListener{
	private double sensitivity=0.01;
	private Point2d lastMove;

	public enum State{
		IN_GAME, // means active in game : click will result in block addition etc...
		NAV_MENU // means unactive in game : pointers appear to navigate in menu
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
		setState(State.NAV_MENU);
		UiEventMediator.getUiEventMediator().addListener(this);
	}
	private void getUpdate(){
		if (state == State.IN_GAME){
			lastMove.x = sensitivity*org.lwjgl.input.Mouse.getDX() ; 
			lastMove.y = sensitivity*org.lwjgl.input.Mouse.getDY();
		}
	}

	public void getMouseEvent(){

		if (state == State.IN_GAME){
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
		switch (state) {
		case IN_GAME:
			org.lwjgl.input.Mouse.setGrabbed(true);
			break;

		case NAV_MENU:
			org.lwjgl.input.Mouse.setGrabbed(false);
			
		default:
			break;
		}
	}
	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventMenu){
			UiEventMenu eMenu = (UiEventMenu) e;
			switch (eMenu) {
			case START_NEW_GAME: case RESUME_GAME:
				setState(State.IN_GAME);
				break;

			case NAV_MENU_OPTIONS:
				setState(State.NAV_MENU);
			default:
				break;
			}
		}
		
		if (e instanceof UiEventKeyboardPressed){
			UiEventKeyboardPressed ePressed = (UiEventKeyboardPressed) e;
			if (ePressed.key == KeyboardBinding.KEYBOARD_MENU){
				setState(State.NAV_MENU);
			}
		}
		
	}

}
