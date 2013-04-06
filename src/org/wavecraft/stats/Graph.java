package org.wavecraft.stats;

public class Graph {
	private double[] data;
	private double[] time;
	private int currPos;
	
	public Graph(int nVal){
		data = new double[nVal];
		time = new double[nVal];
		currPos = 0;
	}

	public void putValueAtTime(double val,double t){
		data[currPos] = val;
		time[currPos] = t;
		currPos++;
		if (currPos==data.length) {currPos=0;}
	}
	
	public int getLength(){
		return data.length;
	}
	
	public double getValue(int k){
		int pos = currPos - k;
		if (pos<0) {
			pos += data.length;
		}
		return data[pos];
	}
	
	public double getTime(int k){
		int pos = currPos - k;
		if (pos<0) {
			pos += data.length;
		}
		return time[pos];
	}
}
