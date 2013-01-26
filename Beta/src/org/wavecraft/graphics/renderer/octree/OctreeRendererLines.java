package org.wavecraft.graphics.renderer.octree;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.octree.Octree;


import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.OctreeStateNotYetVisited;
import org.wavecraft.graphics.texture.MegaTexture;
import org.wavecraft.modif.ModifOctree;

import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;



// singleton
public class OctreeRendererLines implements UiEventListener {
	private static OctreeRendererLines octreeRendererLines;
	private static DrawMode drawMode;
	private enum DrawMode{
		DRAWALL,
		DRAWLEAF,
		DRAWNOTHING,
		TEXTURED,
		MODIF;
	}

	private OctreeRendererLines(){
		drawMode = DrawMode.DRAWNOTHING;
		UiEventMediator.addListener(this);
	}

	public static OctreeRendererLines getInstance(){
		if (octreeRendererLines == null){
			octreeRendererLines = new OctreeRendererLines();
		}
		return octreeRendererLines;
	}



	private static void inner(ModifOctree modifOctree, int J){

		if (drawMode == DrawMode.MODIF && modifOctree.getJ() >= J){
			if (modifOctree.getJ() == J){
				BlockColorerLines.getInstance().setColor(modifOctree);
				BlockRendererLines.getInstance().afterGLLines(modifOctree);
			}
			if (modifOctree.sons != null){
				for (int offset = 0; offset<8 ; offset ++){
					if (modifOctree.sons[offset] != null){
						inner(modifOctree.sons[offset],J);
					}
				}
			}
		}
	}

	public static void renderModif(ModifOctree modifOctree){
		for (int J=0; J <=  Octree.JMAX; J++){
			setLineWidth(J);
		GL11.glBegin(GL11.GL_LINES);
		inner(modifOctree, J);
		GL11.glEnd();
		}
	}
	
	private static void setLineWidth(int J){
		GL11.glLineWidth((float) (3*(J-1)+1) );
	}

	private static void inner(Octree octree,int J){
		if (octree.getJ() == J){

			switch (drawMode) {

			case DRAWALL:
				BlockColorerLines.getInstance().setColor(octree);
				BlockRendererLines.getInstance().afterGLLines(octree);
				break;

			case DRAWLEAF :
				BlockColorerLines.getInstance().setColor(octree);
				if (octree.getState() instanceof OctreeStateLeaf || octree.getState() instanceof OctreeStateNotYetVisited)
					BlockRendererLines.getInstance().afterGLLines(octree);
				break;

			case TEXTURED :
				//GL11.glColor3f(1, 1, 1);
				BlockColorerLines.getInstance().setColor(octree);
				if (octree.getState() instanceof OctreeStateLeaf || octree.getState() instanceof OctreeStateNotYetVisited)
					BlockRendererTexture.afterGLQuads2(octree);
				break;

			case MODIF : 
				break;


			default:
				break;
			}


		}
		if (octree.getJ() >= J){
			if (octree.hasSons()) {
				Octree[] sons = octree.getSons();
				for (int offset = 0 ; offset<8; offset++){
					inner(sons[offset],J);
				}
			}
		}
	}

	public static void render(Octree octree){
		getInstance();
		BlockRendererLines.getInstance();
		for (int J=15;J>=0;J--){
			switch (drawMode) {
			case DRAWALL:
				setLineWidth(J);
				GL11.glBegin(GL11.GL_LINES);
				break;
			case DRAWLEAF :
				GL11.glLineWidth((float) 1);
				GL11.glBegin(GL11.GL_LINES);
				break;
			case TEXTURED :
				MegaTexture.getInstance();
				glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
				glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				MegaTexture.bind();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glColor3d(1, 1, 1);
				break;
			case MODIF :
				GL11.glLineWidth(1);
				GL11.glBegin(GL11.GL_LINES);
			default:
				break;
			}
			//GL11.glBegin(GL11.GL_LINES);
			if (drawMode != DrawMode.DRAWNOTHING){
				inner(octree,J);
			}
			GL11.glEnd();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}



	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){

			UiEventKeyboardPressed eventKeyboard = (UiEventKeyboardPressed) (e);
			switch (eventKeyboard.key) {
			case KEYBOARD_SWITCH_OCTREEDRAW:
				switch (drawMode) {
				case DRAWALL:
					drawMode = DrawMode.DRAWLEAF;
					break;
				case DRAWLEAF:
					drawMode = DrawMode.DRAWNOTHING;
					break;
				case DRAWNOTHING:
					drawMode = DrawMode.TEXTURED;
					break;
				case TEXTURED:
					drawMode = DrawMode.MODIF;
					break;
				case MODIF :
					drawMode = DrawMode.DRAWALL;
					break;
				default:
					break;
				}
			default:
				break;


			}
		}

	}



}
