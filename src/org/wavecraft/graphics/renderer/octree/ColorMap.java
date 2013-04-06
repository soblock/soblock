package org.wavecraft.graphics.renderer.octree;

import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.Coord2d;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;

public class ColorMap {

	private View view ;
	public double[] cm;
	int N;
	private static ColorMap instance;
	
	public static ColorMap getInstance(){
		if (instance == null){
			instance = new ColorMap();
		}
		return instance;
	}
	
	private ColorMap(){
		view = ViewBuilder.viewMenu();
		N = 1024;
		cm = computeJet(N);
	}
	
	public void setColor(double v,double vmin,double vmax){
		int i = (int)((v-vmin)/(vmax-vmin)*N);
		i = Math.min(N-1, Math.max(0, i));
		GL11.glColor3d(cm[3*i], cm[3*i+1], cm[3*i+2]);
	}
	
	public void plotLegend(double[] colormapArray){
		view.initRendering();
		int N = colormapArray.length/3;
		GL11.glBegin(GL11.GL_QUADS);
		for (int i=0;i<N;i++){
			GL11.glColor3d(colormapArray[3*i], colormapArray[3*i+1], colormapArray[3*i+2]);
			Coord2d positionRelative = new Coord2d(-1 + i*2.0/N, -1);
			Coord2d sizeRelative = new Coord2d(2.0/N, 4.0/N);
			double xMin = positionRelative.x;
			double xMax = positionRelative.x+sizeRelative.x;
			double yMin = positionRelative.y;
			double yMax = positionRelative.y+sizeRelative.y;
//			System.out.println(xMin);
//			System.out.println(xMax);
//			System.out.println(yMin);
//			System.out.println(yMax);
//			System.out.println();
//			System.out.println();
			GL11.glVertex2d(xMin, yMin);
			GL11.glVertex2d(xMax, yMin);
			GL11.glVertex2d(xMax, yMax);
			GL11.glVertex2d(xMin, yMax);
		}
		GL11.glEnd();
	}
	
	private static double[] computeJet(int N){
		double[] colormapArray = new double[3*N];
		for (int i =0;i<N;i++){
			int sectionId = (i*8)/N;
			double r = 0;
			double g = 0;
			double b = 0;
			switch (sectionId) {
			case 0:
				b = 0.5 + i*4.0/N;
				break;

			case 1: case 2:
				g = -0.5 + i*4.0/N;
				b = 1;
				break;

			case 3: case 4:
				r = -1.5 + i*4.0/N;
				g = 1;
				b = 2.5 - i*4.0/N;
				break;

			case 5: case 6:
				r = 1;
				g = 3.5 - i*4.0/N;
				break;

			case 7:
				r = 4.5 - i*4.0/N;
				break;	
			default:
				break;
			}
			colormapArray[3*i] = r;
			colormapArray[3*i + 1] = g;
			colormapArray[3*i + 2] = b;
		}
		return colormapArray;
	}
}
