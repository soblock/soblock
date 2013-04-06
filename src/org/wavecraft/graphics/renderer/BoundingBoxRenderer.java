package org.wavecraft.graphics.renderer;



import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.BoundingBox;



public class BoundingBoxRenderer {
	public static void afterGLline(BoundingBox box){
		double xm = box.getMinCoord3d().x;
		double ym = box.getMinCoord3d().y;
		double zm = box.getMinCoord3d().z;
		double xM = box.getMaxCoord3d().x;
		double yM = box.getMaxCoord3d().y;
		double zM = box.getMaxCoord3d().z;
		
		GL11.glVertex3d(xm,ym,zm);
		GL11.glVertex3d(xM,ym,zm);

		GL11.glVertex3d(xM,ym,zm);
		GL11.glVertex3d(xM,ym,zM);

		GL11.glVertex3d(xm,yM,zm);
		GL11.glVertex3d(xM,yM,zm);

		GL11.glVertex3d(xM,yM,zm);
		GL11.glVertex3d(xM,yM,zM);

		GL11.glVertex3d(xm,ym,zm);
		GL11.glVertex3d(xm,yM,zm);

		GL11.glVertex3d(xM,ym,zm);
		GL11.glVertex3d(xM,yM,zm);

		GL11.glVertex3d(xm,ym,zm);
		GL11.glVertex3d(xm,ym,zM);

		GL11.glVertex3d(xm,yM,zm);
		GL11.glVertex3d(xm,yM,zM);

		GL11.glVertex3d(xm,ym,zM);
		GL11.glVertex3d(xM,ym,zM);

		GL11.glVertex3d(xm,yM,zM);
		GL11.glVertex3d(xM,yM,zM);

		GL11.glVertex3d(xm,ym,zM);
		GL11.glVertex3d(xm,yM,zM);

		GL11.glVertex3d(xM,ym,zM);
		GL11.glVertex3d(xM,yM,zM);

		GL11.glVertex3d(xm,ym,zM);
		GL11.glVertex3d(xM,ym,zM);
	}
	
	public static void afterGLquad(BoundingBox box){
		double xm = box.getMinCoord3d().x;
		double ym = box.getMinCoord3d().y;
		double zm = box.getMinCoord3d().z;
		double xM = box.getMaxCoord3d().x;
		double yM = box.getMaxCoord3d().y;
		double zM = box.getMaxCoord3d().z;
		
		GL11.glVertex3d(xm, ym,zm);
		GL11.glVertex3d(xm, yM,zm);
		GL11.glVertex3d(xM, yM,zm);
		GL11.glVertex3d(xM, ym,zm);

		GL11.glVertex3d(xm, ym,zM);
		GL11.glVertex3d(xm, yM,zM);
		GL11.glVertex3d(xM, yM,zM);
		GL11.glVertex3d(xM, ym,zM);

		GL11.glVertex3d(xm, ym,zm);
		GL11.glVertex3d(xm, yM,zm);
		GL11.glVertex3d(xm, yM,zM);
		GL11.glVertex3d(xm, ym,zM);

		GL11.glVertex3d(xM, ym,zm);
		GL11.glVertex3d(xM, yM,zm);
		GL11.glVertex3d(xM, yM,zM);
		GL11.glVertex3d(xM, ym,zM);

		GL11.glVertex3d(xm, ym,zm);
		GL11.glVertex3d(xM, ym,zm);
		GL11.glVertex3d(xM, ym,zM);
		GL11.glVertex3d(xm, ym,zM);

		GL11.glVertex3d(xm, yM,zm);
		GL11.glVertex3d(xM, yM,zm);
		GL11.glVertex3d(xM, yM,zM);
		GL11.glVertex3d(xm, yM,zM);
	}
	
}
