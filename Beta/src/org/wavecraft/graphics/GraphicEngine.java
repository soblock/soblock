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
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.builder.OctreeBuilderWorldFuntionCullerModif;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.graphics.hud.HUD;
import org.wavecraft.graphics.hud.HUDBuilder;
import org.wavecraft.graphics.light.Light;
import org.wavecraft.graphics.renderer.DyadicBlockString;
import org.wavecraft.graphics.renderer.GameObjectRenderer;
import org.wavecraft.graphics.renderer.GameObjectRendererBuilder;
import org.wavecraft.graphics.renderer.octree.BlockRendererLines;
import org.wavecraft.graphics.renderer.octree.BlocktreeRendererLines;
import org.wavecraft.graphics.renderer.octree.OctreeRendererLines;
import org.wavecraft.graphics.texture.MegaTexture;
import org.wavecraft.graphics.vbo.LightFace;
import org.wavecraft.graphics.vbo.VBOBlocktreePool;
import org.wavecraft.graphics.vbo.VBOFace;
import org.wavecraft.graphics.vbo.VBOWrapper.VboMode;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;
import org.wavecraft.graphics.view.WindowSize;
import org.wavecraft.modif.BlocktreeGrabber;
import org.wavecraft.modif.ModifAdder;
import org.wavecraft.modif.ModifAdderBlocktree;
import org.wavecraft.modif.ModifOctree;
import org.wavecraft.stats.Profiler;
import org.wavecraft.ui.menu.MenuSelectBlock;

// this class is the graphic engine
// singleton
public class GraphicEngine {
	private static GraphicEngine graphicEngine ;
	private static View viewMain, viewMinimap;
	private static GameObjectRenderer gameObjectRenderer;
	private static HUD hud;
	private static Light light;



	static ArrayList<Octree> octreeArrMsg ;

	private static VBOFace vboFace = null;

	private GraphicEngine() {
		viewMain = ViewBuilder.viewFullFrameFPS();
		// viewMain = ViewBuilder.viewFullFrameThirdPerson();
		viewMinimap = ViewBuilder.viewSmallFrameMinimap();
		gameObjectRenderer = GameObjectRendererBuilder.defaultGameObjectRenderer();
		hud = HUDBuilder.defaultHUD();
		light = new Light();
		// vboFace = new VBOFace(3*64000); // can go up to 6 * 64000 on macbook
		// pro
		vboFace = new VBOFace(8096, 1, VboMode.V3N3T2); // can go up to 6 *
		//vboFace = new VBOFace(8096, 1, VboMode.V3N3T2); // can go up to 6 *
		// 64000 on macbook
		// pro
		OctreeEventMediator.addListener(vboFace);
		MegaTexture.getInstance().loadTexture();

		WindowSize.getInstance();

		Coord3i ci = new Coord3i(300, 150, 250);

		

		octreeArrMsg = DyadicBlockString.stringToOctreeArr("welcome in beautiful wavecraft",2,ci);

		for (int i = 0 ;i<octreeArrMsg.size();i++){
			vboFace.pushNode(octreeArrMsg.get(i));
		}


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


		Profiler.getInstance().display();
		// GL11.glFlush();
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



		Octree octree = new Octree(0, 0, 0, 0);
		octree.setContent(8);
		OctreeRendererLines.render(octree);

		gameObjectRenderer.render(GameEngine.getPlayer());
		// light.setPositionSunLight();


		BlockRendererLines.getInstance().renderEmphasize(ModifAdder.getNodeToRemove(), 0);
		BlockRendererLines.getInstance().renderEmphasize(ModifAdder.getNodeToAdd(), 1);


		Blocktree nearest = BlocktreeGrabber.nearestIntersectedLeaf(GameEngine.getBlocktree(), GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		//BlockRendererLines.getInstance().renderEmphasize(nearest, 0);
		
		
		BlockRendererLines.getInstance().renderEmphasize(ModifAdderBlocktree.getNodeToRemove(), 0);
		BlockRendererLines.getInstance().renderEmphasize(ModifAdderBlocktree.getNodeToAdd(), 1);



		OctreeRendererLines.render(GameEngine.getOctree());
		if (GameEngine.getOctreeBuilder() instanceof OctreeBuilderWorldFuntionCullerModif){
			ModifOctree modif = ((OctreeBuilderWorldFuntionCullerModif) GameEngine.getOctreeBuilder()).getModifOctree();
			OctreeRendererLines.renderModif(modif);
		}

		//light.initLight();
		light.setSkyColor();
		vboFace.draw();



		VBOBlocktreePool.getInstance().render();




		Light.disableLights();

		light.draw();



	}

	public static void update() {
		double t1 = System.currentTimeMillis();
		vboFace.update();
		VBOBlocktreePool.getInstance().unloadAll();
		VBOBlocktreePool.getInstance().uploadAll();
		double t2 = System.currentTimeMillis();
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
