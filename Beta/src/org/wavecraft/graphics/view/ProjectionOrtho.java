package org.wavecraft.graphics.view;

import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventWindowResized;
import org.lwjgl.opengl.GL11;


public class ProjectionOrtho implements Projection {

	private double xmr = 0;
	private double xMr = 1;
	private double ymr = 0;
	private double yMr = 1;
	private double radius = 32;
	private double znear = 0.1f;
	private double zfar = 8192;

	public ProjectionOrtho(){
	}

	public ProjectionOrtho(double xmr,double xMr, double ymr, double yMr){
		
		this.xmr = xmr;
		this.xMr = xMr;
		this.ymr = ymr;
		this.yMr = yMr;
	}

	public void setRadius(double radius){
		this.radius = radius;
	}

	@Override
	public void setProjectionMatrix() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();	
		GL11.glOrtho(-radius, radius , -radius, radius, znear, zfar);
	}

	@Override
	public void setViewPort() {
		int w =  WindowSize.getInstance().getW();
		int h =  WindowSize.getInstance().getH();
		GL11.glViewport((int) (xmr*w), (int) (ymr*h), (int) ((xMr-xmr)*w),(int) ((yMr-ymr)*h));
	}
	
	

	@Override
	public int[] getViewPortDim() {
		int w =  WindowSize.getInstance().getW();
		int h =  WindowSize.getInstance().getH();
		int dim[] = {(int) ((xMr-xmr)*w),(int) ((yMr-ymr)*h)};
		return dim;
	}

}
