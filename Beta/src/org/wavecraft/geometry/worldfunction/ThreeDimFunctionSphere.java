package org.wavecraft.geometry.worldfunction;


import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimFunctionSphere implements ThreeDimFunction{

	private Coord3d center;
	private double radius;
	
	public ThreeDimFunctionSphere(Coord3d center, double radius){
		this.center = center;
		this.radius = radius;
	}
	@Override
	public double valueAt(Coord3d coord) {
		return coord.distance(center) - radius;
	}

	@Override
	 public double uncertaintyBound(DyadicBlock block) {

		//block is fully inside : we are sure to be inside
		if (block.center().distance(center) + block.edgeLentgh() < this.radius ){
			return 0;
		}
		//center of sphere is inside the block
		if (block.center().distance(center) < block.edgeLentgh() ){
			return 1E20;
		}
		
		
		
		return block.edgeLentgh() * Math.sqrt(3)/2;
		// ok as soon as radius large enough (2)... other wise take curvature...
		
	}
	
}
