package org.wavecraft.graphics.renderer.octree;

import static org.lwjgl.opengl.GL11.*;

import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Blocktree.State;



import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

public class BlocktreeRendererLines implements UiEventListener {
	private static BlocktreeRendererLines instance;
	private static DrawMode drawMode;
	private enum DrawMode{
		DRAWALL,
		DRAWLEAF,
		DRAWNOTHING,
	}
	public static BlocktreeRendererLines getInstance(){
		if (instance == null){
			instance = new BlocktreeRendererLines();
		}
		return instance;
	}

	private BlocktreeRendererLines(){
		drawMode = DrawMode.DRAWNOTHING;
		UiEventMediator.getUiEventMediator().addListener(this);
	}


	public static void render(Blocktree root){
		getInstance();
		glBegin(GL_LINES);

		if (drawMode!=DrawMode.DRAWNOTHING){
			renderInner(root);
		}
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

		if (node.hasSons()){
			for (Blocktree son : node.getSons()){
				renderInner(son);
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
