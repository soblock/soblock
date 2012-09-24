package org.wavecraft.stats;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.wavecraft.graphics.hud.TextRenderer;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;
import org.wavecraft.graphics.view.WindowSize;

public class Profiler {

	private static Profiler instance;

	private ArrayList<Graph> graphs;
	private HashMap<String,Integer> legendsMap;
	private ArrayList<String> legends;
	private int nVal = 512;
	private TextRenderer textRenderer;
	private View view;
	private  double timeWindowInMs = 100;
	private  ArrayList<double[] > colormaps;
	
	private double t1;
	
	public double tic(){
		t1 = System.currentTimeMillis();
		return t1;
	}
	
	public double toc(){
		return System.currentTimeMillis() - t1;
	}

	public static Profiler getInstance(){
		if (instance ==null){
			instance = new Profiler();
		}
		return instance;
	}

	private Profiler(){
		textRenderer = new TextRenderer();
		view = ViewBuilder.viewWindowCoord();
		colormaps = new ArrayList<double[]>();
		colormaps.add(new double[]{1,0,0});
		colormaps.add(new double[]{0,1,0});
		colormaps.add(new double[]{0,0,1});
		graphs = new ArrayList<Graph>();
		legendsMap = new HashMap<String, Integer>();
		legends = new ArrayList<String>();
	}

	public void push(String str, double val, double t){
		if (!legendsMap.containsKey(str)){
			Graph graph = new Graph(nVal);
			graphs.add(graph);
			legendsMap.put(str, new Integer(graphs.size()-1));
			legends.add(str);
		}
		Integer position = legendsMap.get(str);
		graphs.get(position.intValue()).putValueAtTime(val, t);
	}

	public void display(){
		view.initRendering();

		int posx = 0;
		int posy = 0;
		int w = WindowSize.getInstance().getW();
		int h = WindowSize.getInstance().getH();
		for (int i = 0;i<nVal; i++){
			posy++;
			posx = 0;
			for (int j = 0;j<graphs.size();j++){
				int lastPosx = posx;
				posx += graphs.get(j).getValue(i)*w / timeWindowInMs;
				
				//posx += 20;
				double[] rgb = colormaps.get(j%colormaps.size());
				if (i ==1){
					textRenderer.setFontColor(rgb[0], rgb[1], rgb[2]);
					String str = legends.get(j);
					textRenderer.drawString(str, w, h, 100*j, 0);
				}
				GL11.glLineWidth(1);
				GL11.glColor3d(rgb[0], rgb[1], rgb[2]);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex2i(lastPosx, h-16-posy);
				GL11.glVertex2i(posx,h-16-posy);
				
				GL11.glEnd();
			}

		}
	}

}
