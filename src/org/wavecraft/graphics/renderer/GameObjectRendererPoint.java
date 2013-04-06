package org.wavecraft.graphics.renderer;

import org.lwjgl.opengl.GL11;
import org.wavecraft.gameobject.GameObject;
import org.wavecraft.geometry.Coord3d;

public class GameObjectRendererPoint implements GameObjectRenderer {

	@Override
	public void render(GameObject movingObject) {
		GL11.glPointSize(2);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glColor3d(1, 1, 1);
		Coord3d pos = movingObject.getPosition();
		GL11.glVertex3d(pos.x, pos.y, pos.z);
		GL11.glEnd();
	}
	
}
