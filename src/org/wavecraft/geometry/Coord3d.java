package org.wavecraft.geometry;



import javax.vecmath.Point3d;


@SuppressWarnings("serial")
public class Coord3d extends Point3d{
	
	public Coord3d(double x,double y,double z){
		super(x,y,z);
	}
	
	public Coord3d(Coord3d p){
		super(p.x,p.y,p.z);
	}
	
	@Override 
	public String toString(){
		return String.format("%f %f %f", x,y,z);
	}

	public Coord3d projectionOxy() {
		return new Coord3d(x,y,0);
	}
	
	public void scale3d(Coord3d scales){
		x = x/scales.x;
		y = y/scales.y;
		z = z/scales.z;
	}
	
	/**
	 * @param toAdd
	 * @return a new Coord3d instance with the sum of the current instance and toAdd
	 */
	public Coord3d addInNewVector(Coord3d toAdd){
		return new Coord3d(this.x + toAdd.x, this.y + toAdd.y, this.z + toAdd.z);
	}
	
	public Coord3d translate(Coord3d trans){
		return new Coord3d(x + trans.x, y +trans.y, z + trans.z);
	}

	/**
	 * 
	 * @param p
	 * @return a new Coord3d instance with the scaled version of p
	 */
	public Coord3d scalarMult(double p) {
		return new Coord3d(p*this.x, p*this.y, p*this.z);
	}
	
	public Coord3d normalize(){
		double norm = Math.sqrt(x*x + y*y + z*z);
		return this.scalarMult(1/norm);
	}
}
