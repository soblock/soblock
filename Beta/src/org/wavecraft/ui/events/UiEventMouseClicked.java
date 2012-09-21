package org.wavecraft.ui.events;

import org.wavecraft.geometry.Coord2d;

public class UiEventMouseClicked implements UiEvent{
	public Coord2d position;
	public int buttonId;
	public boolean isButtonPressed;
	
	public UiEventMouseClicked(Coord2d position,int buttonId, boolean isButtonPressed){
		this.position = position;
		this.buttonId = buttonId;
		this.isButtonPressed = isButtonPressed;
	}
}
