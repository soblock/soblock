package org.wavecraft.graphics;


import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.wavecraft.client.Timer;
import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.Coord3i;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderThreeDimFunModif;
import org.wavecraft.geometry.blocktree.modif.BlocktreeGrabber;
import org.wavecraft.geometry.blocktree.modif.ModifAdderBlocktree;
import org.wavecraft.geometry.blocktree.modif.ModifOctree;

import org.wavecraft.graphics.hud.HUD;
import org.wavecraft.graphics.hud.HUDBuilder;
import org.wavecraft.graphics.light.Light;
import org.wavecraft.graphics.renderer.DyadicBlockString;
import org.wavecraft.graphics.renderer.GameObjectRenderer;
import org.wavecraft.graphics.renderer.GameObjectRendererBuilder;
import org.wavecraft.graphics.renderer.octree.BlockRendererLines;
import org.wavecraft.graphics.renderer.octree.BlocktreeRendererLines;

import org.wavecraft.graphics.texture.MegaTexture;
import org.wavecraft.graphics.vbo.LightFace;
import org.wavecraft.graphics.vbo.VBOBlocktreePool;

import org.wavecraft.graphics.vbo.VBOWrapper.VboMode;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;
import org.wavecraft.graphics.view.WindowSize;

import org.wavecraft.stats.Profiler;
import org.wavecraft.ui.menu.MenuSelectBlock;
import org.wavecraft.ui.menu.twl.MenuController;

// this class is the graphic engine
// singleton
public class GraphicEngine {
	private static GraphicEngine graphicEngine ;
	private static View viewMain, viewMinimap;
	private static GameObjectRenderer gameObjectRenderer;
	private static HUD hud;
	private static Light light;
	private static MenuController menuController;





	private GraphicEngine() {
		viewMain = ViewBuilder.viewFullFrameFPS();
		// viewMain = ViewBuilder.viewFullFrameThirdPerson();
		viewMinimap = ViewBuilder.viewSmallFrameMinimap();
		gameObjectRenderer = GameObjectRendererBuilder.defaultGameObjectRenderer();
		hud = HUDBuilder.defaultHUD();
		light = new Light();
		
		MegaTexture.getInstance().loadTexture();

		WindowSize.getInstance();

		

		



		
		
		menuController = new MenuController();


	}

	public static GraphicEngine getGraphicEngine() {
		if (graphicEngine == null) {
			graphicEngine = new GraphicEngine();
		}
		return graphicEngine;
	}

	public static void render() {
		double t1 = System.currentTimeMillis();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		viewMain.initRendering();


//
//		GL11.glEnable(GL11.GL_FOG);
//		GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR); 
//		GL11.glFogf(GL11.GL_FOG_START, 256.f);
//		GL11.glFogf(GL11.GL_FOG_END, 512.f);
//

		innerRender();

		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		hud.draw();

		MenuSelectBlock.getInstance().draw();
		//MenuSelectColorMap.getInstance().draw();
		//ColorMap.getInstance().plotLegend(ColorMap.getInstance().cm);
		//Console.getInstance().draw();


		// test window view : 
		View viewWindowCoord = ViewBuilder.viewWindowCoord();	
		viewWindowCoord.initRendering();


		//Profiler.getInstance().display();
		// GL11.glFlush();
		menuController.display();
		
		Display.update();
		Display.setVSyncEnabled(false);


		double t2 = System.currentTimeMillis()-t1;
		Profiler.getInstance().push("render", t2,Timer.getCurrT());

		
	}

	public static void innerRender() {
		// GL11.glFlush();
		Grid.draw();
		// GL11.glFlush();

		
		BlocktreeRendererLines.render(GameEngine.getBlocktree());



		
		

		gameObjectRenderer.render(GameEngine.getPlayer());

		
		BlockRendererLines.getInstance().renderEmphasize(ModifAdderBlocktree.getNodeToRemove(GameEngine.getPlayer()), 0);
		BlockRendererLines.getInstance().renderEmphasize(ModifAdderBlocktree.getNodeToAdd(GameEngine.getPlayer()), 1);



		
		
		if (GameEngine.getBlocktreeBuilder() instanceof BlocktreeBuilderThreeDimFunModif){
			 ((BlocktreeBuilderThreeDimFunModif) GameEngine.getBlocktreeBuilder()).getModif();
		}

		//light.initLight();
		light.setSkyColor();



		VBOBlocktreePool.getInstance().render();




		Light.disableLights();

		light.draw();



	}

	public static void update() {
		double t1 = System.currentTimeMillis();
		VBOBlocktreePool.getInstance().unloadAll();
		VBOBlocktreePool.getInstance().uploadAll();
		double t2 = System.currentTimeMillis();
		menuController.refreshDimension();
		
		Profiler.getInstance().push("updateVBO", t2-t1, Timer.getCurrT());
	}

	public static View getViewMain() {
		return viewMain;
	}

	public static View getViewMinimap() {
		return viewMinimap;
	}

	public static HUD getHud() {
		return hud;
	}

}
