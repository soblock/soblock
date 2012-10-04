package org.wavecraft.graphics.renderer;



import org.lwjgl.opengl.GL11;
import org.wavecraft.gameobject.GameObject;
import org.wavecraft.gameobject.GameObjectMovingOriented;

public class GameObjectRendererBoundingBox implements GameObjectRenderer{

	@Override
	public void render(GameObject movingObject) {
		GL11.glLineWidth(4);
		GL11.glPushMatrix();
		
		GL11.glTranslated(movingObject.getPosition().x, movingObject.getPosition().y, movingObject.getPosition().z);
		if (movingObject instanceof GameObjectMovingOriented){
			//GL11.glRotatef((float)(((GameObjectMovingOriented) movingObject).getTheta()*180/Math.PI),0.f, 0.f, 1.f);
		}
			
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3d(0, 0, 1);
		BoundingBoxRenderer.afterGLline(movingObject.getBoundingBox());
		
		GL11.glEnd();
		GL11.glPointSize(10.f);
		GL11.glColor3f(0, 0, 1);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex3d(0., 0., 0.);
		GL11.glEnd();
		GL11.glPopMatrix();
		
	}

}
