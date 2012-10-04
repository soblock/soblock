package org.wavecraft.graphics.view;

public class View {
	Camera camera;
	Projection projection;
	
	public void initRendering(){
		projection.setProjectionMatrix();
		projection.setViewPort();
		camera.setModelViewMatrix();
	}
	
	public int[] getViewPortDim(){
		return projection.getViewPortDim();
	}
}

