package org.wavecraft.graphics.renderer.octree;

import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Face;

public class FaceRendererLines {

	// extremely inefficient rendering. for debug purposes only
	private static void afterGLrender(Face face){
		Coord3d[] coords = face.getVertices();
		int[] indices = {0 , 1, 1 ,2, 2, 3, 3, 0};
		for (int k = 0; k<indices.length; k++){
			GL11.glVertex3d(coords[indices[k]].x, coords[indices[k]].y, coords[indices[k]].z);
		}
	}
	
	public static void render(Face face){
		setLineWidth();
		GL11.glBegin(GL11.GL_LINES);
		//setColor();
		afterGLrender(face);
		GL11.glEnd();
	}
	
	public static void render(Face[] faceArr){
		setLineWidth();
		GL11.glBegin(GL11.GL_LINES);
		setColor();
		for (int i=0; i<faceArr.length ; i++){
			afterGLrender(faceArr[i]);
		}
		GL11.glEnd();
	}
	
	private static void setLineWidth(){
		GL11.glLineWidth(1);
	}
	
	private static void setColor(){
		GL11.glColor3d(1, 1, 1);
	}
}
