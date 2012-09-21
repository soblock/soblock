package org.wavecraft.graphics.renderer;

import org.lwjgl.opengl.GL11;
import org.wavecraft.gameobject.GameObject;
import org.wavecraft.gameobject.GameObjectMovingOriented;

public class GameObjectRendererDraw implements GameObjectRenderer {

	@Override
	public void render(GameObject movingObject) {
		
		GL11.glPushMatrix();
		GL11.glTranslated(movingObject.getPosition().x, movingObject.getPosition().y, movingObject.getPosition().z);
		if (movingObject instanceof GameObjectMovingOriented){
			//GL11.glRotatef((float)(((GameObjectMovingOriented) movingObject).getTheta()*180/Math.PI),0.f, 0.f, 1.f);
		}
		
		GL11.glBegin(GL11.GL_QUADS);
		HumanRenderer.colorLeg();
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.legLeftBB()));
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.legRightBB()));
		HumanRenderer.colorSkin();
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.footLeftBB()));
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.footRightBB()));
		HumanRenderer.colorBody();
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.bodyBB()));
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.armLeftBB()));
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.armRightBB()));
		HumanRenderer.colorSkin();
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.handLeftBB()));
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.handRightBB()));
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.neckBB()));
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.headBB()));
		HumanRenderer.colorSunglass();
		BoundingBoxRenderer.afterGLquad(movingObject.getBoundingBox().relativeBoundingBox(HumanRenderer.sunglassBB()));
		GL11.glEnd();
		
		GL11.glPopMatrix();
		GameObjectRendererBoundingBox bbrender = new GameObjectRendererBoundingBox();
		bbrender.render(movingObject);
	}

}
