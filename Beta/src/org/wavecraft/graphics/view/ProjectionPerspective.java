package org.wavecraft.graphics.view;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventWindowResized;

public class ProjectionPerspective implements Projection,UiEventListener{

	private float fovy=70;
	private float zNear=0.01f;
	private float zFar=1E13f;
	private int xm = 0;
	private int xM = 200;
	private int ym = 0;
	private int yM = 200;
	
	public ProjectionPerspective(){
		UiEventMediator.getUiEventMediator().addListener(this);
	}
	
	@Override
	public void setProjectionMatrix() {
		int w=xM-xm;
		int h=yM-ym;
		float aspect= w / (h * 1.0f);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();	
		GLU.gluPerspective(fovy, aspect, zNear, zFar);
	}

	@Override
	public void setViewPort() {
		GL11.glViewport(xm, ym, xM-xm, yM-ym);
	}
	
	public void handle(UiEvent event) {
		if (event instanceof UiEventWindowResized){
			int margin = 10;
			xm = margin;
			ym = margin ;
			xM = ((UiEventWindowResized) event).w - margin;
			yM = ((UiEventWindowResized) event).h - margin;
		}
	}

	@Override
	public int[] getViewPortDim() {
		int[] dim = {xM-xm, yM-ym};
		return dim;
	}

}
