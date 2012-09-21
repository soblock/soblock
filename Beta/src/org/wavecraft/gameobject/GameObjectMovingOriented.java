package org.wavecraft.gameobject;

import org.wavecraft.geometry.Coord2d;
import org.wavecraft.geometry.Coord3d;



public class GameObjectMovingOriented extends GameObjectMoving {
	private Coord2d angles;
	
	public GameObjectMovingOriented(){
		super();
		angles = new Coord2d(0, 0);
	}
	
	public double getTheta(){
		return angles.x;
	}
	
	public double getPhi(){
		return angles.y;
	}
	
	public void setTheta(double theta){
		angles.x = theta;
	}
	
	public void setPhi(double phi){
		angles.y = phi;
	}
	
	public Coord2d getAngles(){
		return angles;
	}
	
	public Coord3d getVectorOfSight(){
		double ux = Math.cos(angles.x) * Math.cos(angles.y);
		double uy = Math.sin(angles.x) * Math.cos(angles.y);
		double uz = Math.sin(angles.y);
		return new Coord3d(ux, uy, uz);
	}
}
