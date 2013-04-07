package org.wavecraft.graphics.renderer.octree;

import static org.lwjgl.opengl.GL11.*;
import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.modif.ModifOctree;

import org.wavecraft.geometry.worldfunction.ThreeDimFunction;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionFlat;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

// singleton
public class BlockColorerLines implements UiEventListener{

	private static BlockColorerLines instance = null;
	private static ColorMode colorMode  ;
	private ThreeDimFunction functionForColormap;
	private double vminForColormap;
	private double vmaxForColormap;
	enum ColorMode{
		COLORSTATE,
		COLORINTERSECTPLAYERBB,
		COLORCOLORMAP
	}

	public void setFunForColorMap(ThreeDimFunction fun, double vmin, double vmax){
		this.functionForColormap = fun;
		this.vminForColormap = vmin;
		this.vmaxForColormap = vmax;
	}

	private BlockColorerLines(){
		colorMode = ColorMode.COLORSTATE;
		functionForColormap = new ThreeDimFunctionFlat(0);
		vminForColormap = 0;
		
		
		UiEventMediator.getUiEventMediator().addListener(this);
	}
	public static BlockColorerLines getInstance(){
		if (instance == null){
			instance = new BlockColorerLines();
		}
		return instance;
	}


	public void setColor(DyadicBlock block){
		if (block instanceof ModifOctree){
			double val = ((ModifOctree) block).value;
			if (val<0){
				glColor3d(1, 1, 0);
			}
			if (val==0){
				glColor3d(1, 1, 1);
			}
			if (val>0){
				glColor3d(1, 0, 0);
			}

		}
		
		if (block instanceof Blocktree){
			switch (colorMode) {
			case COLORSTATE:
				switch (((Blocktree) block).getState()) {
				case FATHER:
					glColor3d(0,1,0);
					break;
				case DEAD_GROUND:
					glColor3d(0.8,0.6,0.6);
					break;
				case DEAD_AIR:
					glColor3d(0.3,0.3,1);
					break;
				case PATRIARCH:
					glColor3d(0, 0, 0);
					break;
				case GRAND_FATHER:
					glColor3d(0.5, 0.5, 0.5);
					break;
				case LEAF:
					glColor3d(1, 1, 1);
					break;
				}
				break;
			case COLORINTERSECTPLAYERBB :
				if (GameEngine.getPlayer().getTranslatedBoundingBox().intersects(block)){
					glColor3d(1, 0, 0);
				}
				else{
					glColor3d(0.3, 0.3, 0.3);
				}
				break;

			case COLORCOLORMAP :
				double v = functionForColormap.valueAt(block.center());
				ColorMap.getInstance().setColor(v, vminForColormap, vmaxForColormap);
				break;
			default:
				break;
			}

		}
	}
	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){

			UiEventKeyboardPressed eventKeyboard = (UiEventKeyboardPressed) (e);
			switch (eventKeyboard.key) {
			case KEYBOARD_SWITCH_COLOR:
				switch (colorMode) {
				case COLORSTATE:
					colorMode = ColorMode.COLORINTERSECTPLAYERBB;
					break;
				case COLORINTERSECTPLAYERBB:
					colorMode = ColorMode.COLORCOLORMAP;
					break;
				case COLORCOLORMAP:
					colorMode = ColorMode.COLORSTATE;
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
