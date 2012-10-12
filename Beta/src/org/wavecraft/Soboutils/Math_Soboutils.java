package org.wavecraft.Soboutils;


public class Math_Soboutils {

	public static int[] powerOf2;
	public static float[] fpowerOf2;
	public static double[] dpowerOf2;
	
	public Math_Soboutils(){
		powerOf2 = new int[60];
		for (int J=0;J<60;J++){
			powerOf2[J]=(int) Math.pow(2, J);
		}

		fpowerOf2 = new float[60];
		for (int J=0;J<60;J++){
			fpowerOf2[J]=(float) Math.pow(2, J);
		}
		
		dpowerOf2 = new double[60];
		for (int J=0;J<60;J++){
			dpowerOf2[J]= Math.pow(2, J);
		}
	}



	public static int ithbit(int k,int i) {
		int ttjm=(int) (Math.pow(2, i-1));
		return (k%(2*ttjm))/(ttjm); 
	
	}




	public static double[] minMaxValues(double[] values){
		double vMin=1E20;
		double vMax=-1E20;
		for (int i=0;i<values.length;i++){
			vMin=Math.min(values[i],vMin);
			vMax=Math.max(values[i],vMax);
		}
		double[] vminmax ={vMin,vMax};
		return vminmax;
	}

	public static double minValues(double[] values){
		double vMin=1E20;
		for (int i=0;i<values.length;i++){
			vMin=Math.min(values[i],vMin);

		}
		return vMin;
	}

	public static double maxValues(double[] values){
		double vMax=-1E20;
		for (int i=0;i<values.length;i++){

			vMax=Math.max(values[i],vMax);
		}
		return vMax;
	}

}

