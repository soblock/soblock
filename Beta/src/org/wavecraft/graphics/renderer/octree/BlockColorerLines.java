package org.wavecraft.graphics.renderer.octree;

import org.lwjgl.opengl.GL11;
import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeState;
import org.wavecraft.geometry.octree.OctreeStateDead;
import org.wavecraft.geometry.octree.OctreeStateFatherCool;
import org.wavecraft.geometry.octree.OctreeStateFatherWorried;
import org.wavecraft.geometry.octree.OctreeStateGround;
import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.OctreeStateNotYetVisited;
import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.builder.OctreeBuilderWorldFuntionCullerModif;
import org.wavecraft.geometry.worldfunction.ThreeDimContent;
import org.wavecraft.geometry.worldfunction.ThreeDimContentBiome;
import org.wavecraft.geometry.worldfunction.ThreeDimFunction;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionFlat;
import org.wavecraft.geometry.worldfunction.WorldFunction;
import org.wavecraft.geometry.worldfunction.WorldFunctionWrapper;
import org.wavecraft.modif.ModifOctree;
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
		vminForColormap = Math.pow(2, Octree.JMAX);
		OctreeBuilder builder = GameEngine.getOctreeBuilder();
		if (builder instanceof OctreeBuilderWorldFuntionCullerModif){
			WorldFunction wf = ((OctreeBuilderWorldFuntionCullerModif) builder).getWorldFunction();
			if (wf instanceof WorldFunctionWrapper){
				functionForColormap = ((WorldFunctionWrapper) wf).getThreeDimFunction();
				vminForColormap = -64;
				vmaxForColormap = 64;
				
				ThreeDimContent content = ((WorldFunctionWrapper) wf).getThreeDimContent();
				if (content instanceof ThreeDimContentBiome){
					functionForColormap = ((ThreeDimContentBiome) content).humidity;
					vminForColormap = -0.4;
					vmaxForColormap = 0.4;
				}
			}
			
		}
		
		UiEventMediator.addListener(this);
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
				GL11.glColor3d(1, 1, 0);
			}
			if (val==0){
				GL11.glColor3d(1, 1, 1);
			}
			if (val>0){
				GL11.glColor3d(1, 0, 0);
			}

		}
		if (block instanceof org.wavecraft.geometry.octree.Octree){
			OctreeState state = ((org.wavecraft.geometry.octree.Octree) block).getState();
			switch (colorMode) {
			case COLORSTATE:
				if (state instanceof OctreeStateNotYetVisited){
					GL11.glColor3d(0.5,0.5,0.5);
				}
				if (state instanceof OctreeStateLeaf){
					GL11.glColor3d(1,1,1);
				}
				if (state instanceof OctreeStateDead){
					GL11.glColor3d(1,0,0);
				}
				if (state instanceof OctreeStateGround){
					GL11.glColor3d(1,1,0);
				}
				if (state instanceof OctreeStateFatherWorried){
					GL11.glColor3d(1,0.5,0.5);
				}
				if (state instanceof OctreeStateFatherCool){
					GL11.glColor3d(0,1,0);
				}
				break;

			case COLORINTERSECTPLAYERBB :
				if (GameEngine.getPlayer().getTranslatedBoundingBox().intersects(block)){
					GL11.glColor3d(1, 0, 0);
				}
				else{
					GL11.glColor3d(0.3, 0.3, 0.3);
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
