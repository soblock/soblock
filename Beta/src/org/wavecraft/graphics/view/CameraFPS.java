package org.wavecraft.graphics.view;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.wavecraft.geometry.Coord2d;
import org.wavecraft.geometry.Coord3d;

public class CameraFPS implements Camera {
	private Coord3d position;
	private Coord2d angles;
	
	public void setModelViewMatrix() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		float ux = (float) (Math.cos(angles.x) * Math.cos(angles.y));
		float uy = (float) (Math.sin(angles.x) * Math.cos(angles.y));
		float uz = (float) (Math.sin(angles.y));
		float centerx = (float) position.x + ux;
		float centery = (float) position.y + uy;
		float centerz = (float) position.z + uz;
		float upx = (float) 0;
		float upy = (float) 0;
		float upz = (float) 2;
	
		GLU.gluLookAt((float) position.x,(float) position.y, (float) position.z, centerx, centery, centerz, upx, upy,
				upz);
	}
	

	public void setConfig(Coord3d position,Coord2d angles){
		this.position = position;
		this.angles = angles;
	}
	

	@Override
	public String toString(){
		return String.format("%s %s",position.toString(),angles.toString());
	}

}
