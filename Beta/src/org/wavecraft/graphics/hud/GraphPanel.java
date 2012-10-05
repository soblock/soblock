package org.wavecraft.graphics.hud;

import org.lwjgl.opengl.GL11;
import org.wavecraft.client.Timer;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;
import org.wavecraft.stats.Graph;

public class GraphPanel {
	private Graph graph;
	private View view;
	private TextRenderer textRenderer;
	private double maxVal,minVal;
	
	public GraphPanel(Graph graph,double minVal,double maxVal,int i){
		this.graph = graph;
		this.minVal = minVal;
		this.maxVal = maxVal;
		this.view = ViewBuilder.viewGraph(i);
		this.textRenderer = new TextRenderer();
	}
	
	public void draw(){
		view.initRendering();
		GL11.glLineWidth(1);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3d(0,1,0);
		int K = 4;
		for (int k = 0 ; k<=K ; k++){
			GL11.glVertex2d(-1, 2*k/(K*1.01)-0.999);
			GL11.glVertex2d(1, 2*k/(K*1.01)-0.999);
		}
		GL11.glEnd();
		
		int N = graph.getLength();
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3d(0.3,0.3,1);
		for (int k=1;k<N-1;k++){
			GL11.glVertex2d(2*k/(1.0*N)-1, 2*(graph.getValue(k)-minVal)/(maxVal-minVal) - 1 );
			GL11.glVertex2d(2*(k+1)/(1.0*N)-1, 2*(graph.getValue(k+1)-minVal)/(maxVal-minVal) - 1 );
		}
		GL11.glEnd();
		
		
		String str = String.format("%f", graph.getValue(1));
		
		int[] dims = view.getViewPortDim();
		//System.out.format("graph panel viewport dim %d %d %n",dims[0],dims[1]);
		//TextRenderer.drawString(str, 150,100,0 ,0 );
		textRenderer.drawString(str, dims[0],dims[1],0 ,0 );
	}
}
