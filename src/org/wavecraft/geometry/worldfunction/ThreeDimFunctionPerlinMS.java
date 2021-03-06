package org.wavecraft.geometry.worldfunction;

import java.io.Serializable;
import java.util.Random;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

public class ThreeDimFunctionPerlinMS implements ThreeDimFunction, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4164195245194200811L;
	private ThreeDimFunctionPerlin[] perlinArr;

	public ThreeDimFunctionPerlinMS(){
		
		int N = 32;
		Random gener = new Random(0);
		perlinArr = new ThreeDimFunctionPerlin[4];
		
		perlinArr[0] = new ThreeDimFunctionPerlin(gener, N, 1, -0.3/10, 0.3/10);
		perlinArr[1] = new ThreeDimFunctionPerlin(gener, N, 3, -1/10, 1/10);
		perlinArr[2] = new ThreeDimFunctionPerlin(gener, N, 5, -1, 1);
		perlinArr[3] = new ThreeDimFunctionPerlin(gener, N, 7, -2, 2);
		
	}
	
	public ThreeDimFunctionPerlinMS(int J,int N){
		Random gener = new Random(0);
		perlinArr = new ThreeDimFunctionPerlin[J];
		for (int j = 0 ;j<J;j++){
			double minj = -Math.pow(2, j-J -1);
			double maxj = Math.pow(2, j-J -1);
			perlinArr[j] = new ThreeDimFunctionPerlin(gener, N ,j , minj,maxj);
		}
	}
	public ThreeDimFunctionPerlinMS(int J,int N, double minv, double maxv){
		Random gener = new Random(0);
		perlinArr = new ThreeDimFunctionPerlin[J];
		for (int j = 0 ;j<J;j++){
			double minj = Math.pow(2, j)/((-1+Math.pow(2,J)))*minv;
			double maxj = Math.pow(2, j)/((-1+Math.pow(2,J)))*maxv;
			perlinArr[j] = new ThreeDimFunctionPerlin(gener, N ,j , minj,maxj);
		}
	}
	
	public ThreeDimFunctionPerlinMS(int J,int N, double minv, double maxv, int seed){
		Random gener = new Random(seed);
		perlinArr = new ThreeDimFunctionPerlin[J];
		for (int j = 0 ;j<J;j++){
			double minj = Math.pow(2, j)/((-1+Math.pow(2,J)))*minv;
			double maxj = Math.pow(2, j)/((-1+Math.pow(2,J)))*maxv;
			perlinArr[j] = new ThreeDimFunctionPerlin(gener, N ,j , minj,maxj);
		}
	}
	
	
	public double valueAt(Coord3d coord) {
		double value = 0;
		for (int i =0;i<perlinArr.length; i++) {
			value += perlinArr[i].valueAt(coord);
		}
		return value;
	}

	@Override
	public double uncertaintyBound(DyadicBlock block) {
		double bound = 0;
		for (int i =0;i<perlinArr.length; i++) {
			bound += perlinArr[i].uncertaintyBound(block);
		}
		return bound;
	}
		
}
