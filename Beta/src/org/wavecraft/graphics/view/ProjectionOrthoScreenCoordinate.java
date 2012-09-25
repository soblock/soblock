package org.wavecraft.graphics.view;

import org.lwjgl.opengl.GL11;

// use this class to define projection matrix that fits the screen coordinate
// singleton
public class ProjectionOrthoScreenCoordinate implements Projection {
	private static ProjectionOrthoScreenCoordinate instance;
	
	public static ProjectionOrthoScreenCoordinate getInstance(){
		if (instance == null){
			instance = new ProjectionOrthoScreenCoordinate();
		}
		return instance;
	}
	
	private ProjectionOrthoScreenCoordinate(){
	}

	@Override
	public void setProjectionMatrix() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();	
		int w = WindowSize.getInstance().getW();
		int h = WindowSize.getInstance().getH();
		double left = 0;
		double right = w;
		double bottom = 0;
		double top = h;
		double znear = -8096;
		double zfar = 8096;
		GL11.glOrtho(left, right , bottom, top, znear, zfar);
	}

	@Override
	public void setViewPort() {
		// TODO Auto-generated method stub
		int w =  WindowSize.getInstance().getW();
		int h =  WindowSize.getInstance().getH();
		GL11.glViewport(0,0,w,h);

	}

	@Override
	public int[] getViewPortDim() {
		// TODO Auto-generated method stub
		return null;
	}

}
