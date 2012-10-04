package org.wavecraft.graphics.view;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.wavecraft.geometry.Coord2d;
import org.wavecraft.geometry.Coord3d;

public class CameraThirdPerson implements Camera {
	private Coord3d position;
	private Coord2d angles;
	private double distance = 5;
	
	public void setModelViewMatrix() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
				
		float ux = (float) (Math.cos(angles.x) );
		float uy = (float) (Math.sin(angles.x) );
		float uz = (float) (Math.sin(angles.y));

		float centerx = (float) position.x + ux;
		float centery = (float) position.y + uy;
		float centerz = (float) position.z + uz;
		float upx = (float) 0;
		float upy = (float) 0;
		float upz = (float) 2;
	
		GLU.gluLookAt((float) (position.x - distance*ux),(float) (position.y- distance*uy), (float) (position.z- distance*uz),
				centerx, centery, centerz, upx, upy,
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
