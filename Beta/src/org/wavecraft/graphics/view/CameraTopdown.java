package org.wavecraft.graphics.view;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.wavecraft.geometry.Coord3d;

public class CameraTopdown implements Camera{

	private Coord3d position;
	private double z = 4096;
	
	public void setPosition(Coord3d position){
		this.position = position;
	}
	
	@Override
	public void setModelViewMatrix() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		float centerx = (float) position.x + 0;
		float centery = (float) position.y + 0;
		float centerz = (float) z-1;
		float upx = (float) 0;
		float upy = (float) 1;
		float upz = (float) 0;
		
		GLU.gluLookAt((float) position.x,(float) position.y, (float) z, centerx, centery, centerz, upx, upy,
				upz);
		
	}

}
