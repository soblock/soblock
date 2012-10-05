package org.wavecraft.graphics.renderer.octree;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.fluid.FluidTree;

public class FluidTreeRenderer {
	
	public FluidTreeRenderer(){
	
	}


	// extremly inefficent, for debug only
	public static void renderTexture(FluidTree fluidTree){
		GL11.glColor3d(1, 1, 1);
		ArrayList<DyadicBlock> arrayTree = fluidTree.rasterize();
		BlockRendererTexture.render(arrayTree);
	}
}
