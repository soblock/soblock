package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;



public class ThreeDimFunctionSinc implements ThreeDimFunction {
	Coord3d center;
	double scale;
	double deltaz;
	double Z0;
	
	
	public ThreeDimFunctionSinc(Coord3d center,double scale, double deltaz,double Z0){
		this.center=center;
		this.scale=scale;
		this.deltaz=deltaz;
		this.Z0=Z0;
		
		
	}
	
	
	double sinc(double x){
		if (Math.abs(x)<1E-10)
			return 1;
		else
			return Math.sin(x)/x;
	}
	
	@Override
	public double valueAt(Coord3d coord) {
		double r=center.projectionOxy().distance(coord.projectionOxy()); 
		return (coord.z - Z0) - deltaz*sinc(r/scale);
	}

	
	double derivSinc(double x){
		if (Math.abs(x)<1E-10)
			return 0;
		else
			return Math.cos(x)/x- Math.sin(x)/(x*x);
	}
	
	double maxGradienOfSinc(){
		//numerically, the maximum of gradient of sinc funciton is 0.4362
		
		return 0.5*deltaz/scale;
	}
	
	
	@Override
	public double uncertaintyBound(DyadicBlock block) {
		return block.edgeLentgh()* (1+maxGradienOfSinc());
	}


	

}
