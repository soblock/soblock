package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimFunctionFlat implements ThreeDimFunction{

	private double z0;
	
	public ThreeDimFunctionFlat(double z0){
		this.z0 = z0;
	}
	@Override
	public double valueAt(Coord3d coord) {
		return coord.z - z0;
	}

	@Override
	public double uncertaintyBound(DyadicBlock block) {
		//return block.edgeLentgh();
		return 0;
	}
	


}
