package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimFunctionSum implements ThreeDimFunction{

	private ThreeDimFunction fun1;
	private ThreeDimFunction fun2;
	
	public ThreeDimFunctionSum(ThreeDimFunction fun1, ThreeDimFunction fun2){
		this.fun1 = fun1;
		this.fun2 = fun2;
	}
	
	@Override
	public double valueAt(Coord3d coord) {
		return fun1.valueAt(coord) + fun2.valueAt(coord);
	}


	@Override
	public double uncertaintyBound(DyadicBlock block) {
		return fun1.uncertaintyBound(block) + fun2.uncertaintyBound(block);
	}

}
