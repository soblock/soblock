package org.wavecraft.geometry;

import javax.vecmath.Point2d;

@SuppressWarnings("serial")
public class Coord2d extends Point2d{

	public Coord2d(double x,double y){
		super(x,y);
	}
	
	@Override
	public String toString(){
		return String.format("%f %f", x,y);
	}
}
