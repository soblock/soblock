package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimFunctionUtils{
	public static double[] valueAtPoints(ThreeDimFunction fun,Coord3d[] points){
		int N = points.length;
		double[] val = new double[N];
		for (int i=0; i<N;i++){
			val[i] = fun.valueAt(points[i]);
		}
		return val;
	}
	
	public static double[] minMax(double[] val){
		double min = 1E20;
		double max = -1E20;
		double currVal;
		int N = val.length;
		for (int i = 0 ; i<N ; i++){
			currVal = val[i];
			max = Math.max(currVal, max);
			min = Math.min(currVal, min);
		}
		return new double[]{min,max};
	}
	
	public static double[] minMaxValuesAtVertices(ThreeDimFunction fun, DyadicBlock block){
		Coord3d[] ver = block.vertices();
		double[] val = valueAtPoints(fun, ver);
		return minMax(val);
	}
}
