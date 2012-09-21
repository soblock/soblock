package org.wavecraft.ui.events;

import javax.vecmath.Point2d;

public class UiEventMouseMoved implements UiEvent{
	public Point2d move;
	
	public UiEventMouseMoved(Point2d move){
		this.move = move;
	}
}
