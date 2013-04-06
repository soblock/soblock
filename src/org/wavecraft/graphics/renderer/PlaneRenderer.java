package org.wavecraft.graphics.renderer;

import static org.lwjgl.opengl.GL11.*;


import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Plane;

public class PlaneRenderer {

	public static void render(Plane plane){
		double range = 2000;
		glBegin(GL_POINTS);
		for (int i = 0; i<10000; i++){
			Coord3d p = new Coord3d(Math.random()*range, Math.random()*range, Math.random()*range);
			Coord3d pProj = plane.projCoord3d(p);
			glVertex3d(pProj.x, pProj.y, pProj.z);
		}
		glEnd();
	}
}
