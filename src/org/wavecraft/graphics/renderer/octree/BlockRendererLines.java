package org.wavecraft.graphics.renderer.octree;

import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.DyadicBlock;

// singleton
public class BlockRendererLines {
	private static BlockRendererLines instance;

	public static BlockRendererLines getInstance(){
		if (instance == null){
			instance = new BlockRendererLines();
		}
		return instance;
	}

	private  BlockRendererLines(){
	}
	
	
	public void render(DyadicBlock block){
		setLineWidth(block);
		GL11.glBegin(GL11.GL_LINES);
		BlockColorerLines.getInstance().setColor(block);
		afterGLLines(block);
		GL11.glEnd();
	}

	public void renderEmphasize(DyadicBlock block,int n){
		if (block !=null){
			GL11.glLineWidth(3+block.getJ());
			GL11.glBegin(GL11.GL_LINES);
			switch (n){
			case 0 :
				GL11.glColor3f(1, 0, 1);
				break;
			case 1 :
				GL11.glColor3f(0, 1, 1);
				break;
			}
			afterGLLines(block);
			GL11.glEnd();
		}
	}

	private void setLineWidth(DyadicBlock block){
		GL11.glLineWidth((float) (block.getJ()));
	}


	public void afterGLLines(DyadicBlock block){
		float width = (float) block.edgeLentgh();
		float x = (float) (width*block.x - 0.05*(block.getJ()+1));
		float y = (float) (width*block.y - 0.05*(block.getJ()+1));
		float z = (float) (width*block.z - 0.05*(block.getJ()+1));
		width = width +  0.1f*(block.getJ()+1);

		GL11.glVertex3f(x,y,z);
		GL11.glVertex3f(x+width,y,z);

		GL11.glVertex3f(x+width,y,z);
		GL11.glVertex3f(x+width,y,z+width);

		GL11.glVertex3f(x,y+width,z);
		GL11.glVertex3f(x+width,y+width,z);

		GL11.glVertex3f(x+width,y+width,z);
		GL11.glVertex3f(x+width,y+width,z+width);

		GL11.glVertex3f(x,y,z);
		GL11.glVertex3f(x,y+width,z);

		GL11.glVertex3f(x+width,y,z);
		GL11.glVertex3f(x+width,y+width,z);

		GL11.glVertex3f(x,y,z);
		GL11.glVertex3f(x,y,z+width);

		GL11.glVertex3f(x,y+width,z);
		GL11.glVertex3f(x,y+width,z+width);

		GL11.glVertex3f(x,y,z+width);
		GL11.glVertex3f(x+width,y,z+width);

		GL11.glVertex3f(x,y+width,z+width);
		GL11.glVertex3f(x+width,y+width,z+width);

		GL11.glVertex3f(x,y,z+width);
		GL11.glVertex3f(x,y+width,z+width);

		GL11.glVertex3f(x+width,y,z+width);
		GL11.glVertex3f(x+width,y+width,z+width);

		GL11.glVertex3f(x,y,z+width);
		GL11.glVertex3f(x+width,y,z+width);
	}

	

}
