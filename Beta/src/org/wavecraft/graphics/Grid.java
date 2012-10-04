package org.wavecraft.graphics;
import org.lwjgl.opengl.GL11;


public class Grid {
	
	private static int N = 2048;
	private static int offset = 16;

	public static void draw(){
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glLineWidth(1);
		GL11.glColor3f(1, 0, 0);
		for (int i=0;i<=N;i+=offset){
			GL11.glColor3f(1, 0, 0);
			GL11.glVertex3i(i,0, 0);
			GL11.glColor3f(0, 1, 0);
			GL11.glVertex3i(i,N, 0);
			GL11.glColor3f(1, 0, 0);
			GL11.glVertex3i(0,i, 0);
			GL11.glColor3f(0, 0, 1);
			GL11.glVertex3i(N,i, 0);
		}
		GL11.glEnd();
////		
//		GL11.glLineWidth(1);
//		GL11.glBegin(GL11.GL_LINES);
//		for (int i = 0;i<=N ; i++)
//			for (int j = 0;j<=N ; j++){
//				GL11.glColor3d(i/(1.0*N), j/(1.0*N), 1 - i/(1.0*N));
//				GL11.glVertex3i(i,j, 0);
//				GL11.glVertex3i(i,j+1, 0);
//				GL11.glVertex3i(i,j, 0);
//				GL11.glVertex3i(i+1,j, 0);
//			}
//		GL11.glEnd();
	
		
	}
}
