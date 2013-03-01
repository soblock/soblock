package org.wavecraft.graphics.renderer.octree;

import static org.lwjgl.opengl.GL11.*;

import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Blocktree.State;



import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

public class BlocktreRendererLines implements UiEventListener {
	private static BlocktreRendererLines instance;
	private static DrawMode drawMode;
	private enum DrawMode{
		DRAWALL,
		DRAWLEAF,
		DRAWNOTHING,
		TEXTURED,
		MODIF;
	}
	public static BlocktreRendererLines getInstance(){
		if (instance == null){
			instance = new BlocktreRendererLines();
		}
		return instance;
	}

	private BlocktreRendererLines(){
		drawMode = DrawMode.DRAWALL;
		UiEventMediator.addListener(this);
	}


	public static void render(Blocktree root){
		getInstance();
		glBegin(GL_LINES);
		renderInner(root);
		glEnd();
	}

	private static void renderInner(Blocktree node){
		BlockColorerLines.getInstance().setColor(node);
		switch (drawMode) {
		case DRAWALL:
			BlockRendererLines.getInstance().afterGLLines(node);
			break;

		case DRAWLEAF:
			if (node.getState() == State.LEAF){
				BlockRendererLines.getInstance().afterGLLines(node);
			}
			break;
		default:
			break;
		}

		if (drawMode!=DrawMode.DRAWNOTHING){
			if (node.hasSons()){
				for (Blocktree son : node.getSons()){
					renderInner(son);
				}
			}
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
