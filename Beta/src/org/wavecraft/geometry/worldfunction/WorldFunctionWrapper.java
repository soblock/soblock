package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Terran;

public class WorldFunctionWrapper implements WorldFunction {

	private ThreeDimContent content;
	private ThreeDimFunction fun;

	public WorldFunctionWrapper(ThreeDimContent content, ThreeDimFunction fun) {
		this.content = content;
		this.fun = fun;
	}

	public ThreeDimFunction getThreeDimFunction(){
		return fun;
	}
	
	public ThreeDimContent getThreeDimContent(){
		return content;
	}
	
	@Override
	public Terran contentAt(DyadicBlock block) {
		return content.contentAt(block);
	}

	@Override
	public double valueAt(Coord3d coord) {
		return fun.valueAt(coord);
	}

	@Override
	public double uncertaintyBound(DyadicBlock block) {
		return fun.uncertaintyBound(block);
	}

}
