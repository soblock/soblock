package org.wavecraft.geometry.worldfunction;

import java.io.Serializable;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimFunctionNoisyFlat implements ThreeDimFunction, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7458689788854365324L;
	private ThreeDimFunctionPerlinMS noise;
	private double z0;
	private double deltaz;
	
	public ThreeDimFunctionNoisyFlat(double z0, double deltaz){
		this.deltaz = deltaz;
		noise = new ThreeDimFunctionPerlinMS();
		this.z0 = z0;
	}
	
	@Override
	public double valueAt(Coord3d coord) {
		return coord.z + deltaz*noise.valueAt(coord) - z0;
	}
	@Override
	public double uncertaintyBound(DyadicBlock block) {
		return deltaz*noise.uncertaintyBound(block) ;
	}
}
