package org.wavecraft.geometry;

/**
 * 
 * @author laurentsifre
 * This class is used by the view frustrum culler
 */
public class Plane {

	/**
	 * the plane equation is a*x + b*y + c*z + d = 0
	 * coefficients are normalized such that if the sum is evaluated
	 * on a vector that does not belong to the plane, the sum return the oriented distance 
	 */
	private final double a,b,c,d;
	
	/**
	 * construct a plane from a point that belongs to the plane and the normal of the plane.
	 * @param point
	 * @param orientedNormal : the sign is important and will define the oriented distance
	 */
	public Plane(Coord3d point, Coord3d orientedNormal){
		// if A = (xa, xb, xc) belongs to the plane P and N is its normal,
		// then B = (x, y, z) belongs to the plane if P iff
		// AB.N = 0;
		// i.e. (xa-x).xn + ... = 0
		// thus
		orientedNormal = orientedNormal.normalize();
		a = orientedNormal.x;
		b = orientedNormal.y;
		c = orientedNormal.z;
		d = - (point.x * orientedNormal.x + point.y * orientedNormal.y + point.z * orientedNormal.z);
	}
	
	public double orientedDistance(Coord3d point){
		return a*point.x + b*point.y + c*point.z + d;
	}
	
	public Coord3d projCoord3d(Coord3d p){
		double dis = orientedDistance(p);
		return p.addInNewVector(getNormal().scalarMult(-dis));
	}
	
	public Coord3d getNormal(){
		return new Coord3d(a, b, c);
	}
}
