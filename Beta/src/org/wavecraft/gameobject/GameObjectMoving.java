package org.wavecraft.gameobject;

import org.wavecraft.geometry.Coord3d;

public class GameObjectMoving extends GameObject{
	
	protected Coord3d speed;
	
	public GameObjectMoving(){
		super();
		this.speed=new Coord3d(0, 0, 0);
	}
	
	public void moove(double dt){
		position.scaleAdd(dt, speed, position);
	}
	
	public void setSpeed(Coord3d speed){
		this.speed = speed; 
	}
	
	
}
