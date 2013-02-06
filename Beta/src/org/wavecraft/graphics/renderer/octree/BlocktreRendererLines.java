package org.wavecraft.graphics.renderer.octree;

import static org.lwjgl.opengl.GL11.*;

import org.wavecraft.geometry.blocktree.Blocktree;

public class BlocktreRendererLines {

	public static void render(Blocktree root){
		glBegin(GL_LINES);
		renderInner(root);
		glEnd();
	}
	
	private static void renderInner(Blocktree node){
		BlockColorerLines.getInstance().setColor(node);
		BlockRendererLines.getInstance().afterGLLines(node);
		if (node.hasSons()){
			for (Blocktree son : node.getSons()){
				renderInner(son);
			}
		}
	}

}
