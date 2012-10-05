package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public interface ThreeDimFunction {
	public double valueAt(Coord3d coord);
	public double uncertaintyBound(DyadicBlock block);
}
