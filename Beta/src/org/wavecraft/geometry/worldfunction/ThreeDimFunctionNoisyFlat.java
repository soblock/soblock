package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimFunctionNoisyFlat implements ThreeDimFunction {

	private ThreeDimFunctionPerlinMS noise;
	private double z0;
	
	public ThreeDimFunctionNoisyFlat(double z0){
		noise = new ThreeDimFunctionPerlinMS();
		this.z0 = z0;
	}
	
	@Override
	public double valueAt(Coord3d coord) {
		return coord.z + 5*noise.valueAt(coord) - z0;
	}
	@Override
	public double uncertaintyBound(DyadicBlock block) {
		return noise.uncertaintyBound(block);
	}
}
