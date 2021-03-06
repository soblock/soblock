package org.wavecraft.geometry.worldfunction;

import java.io.Serializable;
import java.util.Random;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;

@SuppressWarnings("serial")
public class ThreeDimFunctionPerlin implements ThreeDimFunction, Serializable {
	private int[] perm;
	private double[] noise;
	private int N;
	private double j; 
	private double minVal,maxVal;
	
	public ThreeDimFunctionPerlin(){
		Random gener = new Random(0);
		this.N = 32;
		this.j = 7;
		this.perm = randomPermutation(N, gener);
		this.noise = noise1d(N, gener);
		this.minVal = -1;
		this.maxVal = 7;
	}
	
	public ThreeDimFunctionPerlin(Random gener,int N,int j, double minVal, double maxVal){
		this.N = N;
		this.j = j;
		this.minVal = minVal;
		this.maxVal = maxVal;
		this.perm = randomPermutation(N, gener);
		this.noise = noise1d(N, gener);
	}
	
	
	private static int[] randomPermutation(int N,Random gener){
		int[] p=new int[N];
		for (int i=0;i<N;i++){
			p[i]=-1;
		}
		for (int i=0;i<N;i++){
			int nextr=gener.nextInt(N);
			while (p[nextr]>-1){
				nextr++;
				nextr=nextr%N;
			}
			p[nextr]=i;
		}
		return p;
	}
	
	private static double[] noise1d(int N,Random gener){
		double[] noise=new double[N];
		for (int i=0;i<N;i++){
			noise[i]=gener.nextDouble();
		}
		return noise;
	}

	private double pseudoRandomInt(int i,int j,int k){
		i = periodize(i, N);
		j = periodize(j, N);
		k = periodize(k, N);
		int ind=perm[(i+perm[(j+perm[k % N]) % N]) % N];
		return noise[ind];
	}
	
	public static int periodize(int i, int per){
		return (i<0)? (i+ per*((-i/per)+1))%per : i%per;
	}
	
	private double pseudoRandomDouble(double x,double y,double z,double scaling){
		// linear interpolation 
		double X=x/scaling;
		double Y=y/scaling;
		double Z=z/scaling;
		int i=(int) (Math.floor(X));
		int j=(int) (Math.floor(Y));
		int k=(int) (Math.floor(Z));
		
		double dx=(X - i);
		double dy=(Y - j);
		double dz=(Z - k);
		
		return pseudoRandomInt(i  ,j  ,k  )*(1-dx)*(1-dy)*(1-dz)
		      +pseudoRandomInt(i+1,j  ,k  )*(  dx)*(1-dy)*(1-dz)
		      +pseudoRandomInt(i  ,j+1,k  )*(1-dx)*(  dy)*(1-dz)
		      +pseudoRandomInt(i+1,j+1,k  )*(  dx)*(  dy)*(1-dz)
		      +pseudoRandomInt(i  ,j  ,k+1)*(1-dx)*(1-dy)*(  dz)
		      +pseudoRandomInt(i+1,j  ,k+1)*(  dx)*(1-dy)*(  dz)
		      +pseudoRandomInt(i  ,j+1,k+1)*(1-dx)*(  dy)*(  dz)
		      +pseudoRandomInt(i+1,j+1,k+1)*(  dx)*(  dy)*(  dz);
	}
	
	
	@Override
	public double valueAt(Coord3d coord) {
		double val = minVal + (maxVal - minVal) * pseudoRandomDouble(coord.x,coord.y,coord.z,Math.pow(2, j));
		return val;
	}

	@Override
	public double uncertaintyBound(DyadicBlock block) {
		
		if (j<block.getJ()){
			return maxVal - minVal;
		}
		else {
			return 0;
		}
		
		//return block.edgeLentgh()*(maxVal-minVal)/Math.pow(2, j);
		
	}
	

}
