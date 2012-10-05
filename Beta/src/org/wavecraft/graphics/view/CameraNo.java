package org.wavecraft.graphics.view;

import org.lwjgl.opengl.GL11;

public class CameraNo implements Camera{

	@Override
	public void setModelViewMatrix() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

}
