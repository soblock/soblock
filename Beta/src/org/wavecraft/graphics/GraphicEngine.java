package org.wavecraft.graphics;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.GLUtils;
import org.wavecraft.client.Timer;
import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Coord3i;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeUtils;
import org.wavecraft.geometry.octree.builder.OctreeBuilderWorldFuntionCullerModif;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.graphics.hud.HUD;
import org.wavecraft.graphics.hud.HUDBuilder;
import org.wavecraft.graphics.light.Light;
import org.wavecraft.graphics.renderer.DyadicBlockString;
import org.wavecraft.graphics.renderer.GameObjectRenderer;
import org.wavecraft.graphics.renderer.GameObjectRendererBuilder;
import org.wavecraft.graphics.renderer.octree.BlockRendererLines;
import org.wavecraft.graphics.renderer.octree.BlockRendererTexture;
import org.wavecraft.graphics.renderer.octree.ColorMap;
import org.wavecraft.graphics.renderer.octree.FaceRendererLines;
import org.wavecraft.graphics.renderer.octree.OctreeRendererLines;
import org.wavecraft.graphics.texture.CharacterTexture;
import org.wavecraft.graphics.texture.MegaTexture;
import org.wavecraft.graphics.vbo.FaceToArray;
import org.wavecraft.graphics.vbo.VBOFace;
import org.wavecraft.graphics.vbo.VBOWrapper;
import org.wavecraft.graphics.vbo.VBOWrapper.VboMode;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;
import org.wavecraft.graphics.view.WindowSize;
import org.wavecraft.modif.BlockGrabber;
import org.wavecraft.modif.ModifAdder;
import org.wavecraft.modif.ModifOctree;
import org.wavecraft.stats.Profiler;
import org.wavecraft.ui.menu.Console;
import org.wavecraft.ui.menu.MenuSelectColorMap;
import org.wavecraft.ui.menu.MenuSelectBlock;

// this class is the graphic engine
// singleton
public class GraphicEngine {
	private static GraphicEngine graphicEngine = null;
	private static View viewMain, viewMinimap;
	private static GameObjectRenderer gameObjectRenderer = null;
	private static HUD hud;
	private static Light light;

	static ArrayList<Octree> octreeArrMsg ;

	private static VBOFace vboFace = null;

	private GraphicEngine() {
		viewMain = ViewBuilder.viewFullFrameFPS();
		// viewMain = ViewBuilder.viewFullFrameThirdPerson();
		viewMinimap = ViewBuilder.viewSmallFrameMinimap();
		gameObjectRenderer = GameObjectRendererBuilder
		.defaultGameObjectRenderer();
		hud = HUDBuilder.defaultHUD();
		light = new Light();
		// vboFace = new VBOFace(3*64000); // can go up to 6 * 64000 on macbook
		// pro
		vboFace = new VBOFace(8096, 32, VboMode.V3N3T2); // can go up to 6 *
		// 64000 on macbook
		// pro
		OctreeEventMediator.addListener(vboFace);
		MegaTexture.getInstance();

		WindowSize.getInstance();

		Coord3i ci = new Coord3i(300, 150, 250);



		octreeArrMsg = DyadicBlockString.stringToOctreeArr("welcome in beautiful wavecraft",2,ci);

		for (int i = 0 ;i<octreeArrMsg.size();i++){
			vboFace.pushNode(octreeArrMsg.get(i));
		}

		// vboTest = new VBOWrapper(VboMode.V3N3T2);
		// float[] initArrayForVbo = {
		// // x y z nx ny nz tx ty
		// 0, 0, 0, 0, 0, 1, 0, 0,
		// 1, 0, 0, 0, 0, 1, 1, 0,
		// 1, 1, 0, 0, 0, 1, 1, 1,
		// 0, 1, 0, 0, 0, 1, 0, 1,
		// };
		// vboTest.initFromFloat(initArrayForVbo);

		//CharacterTexture.getInstance().printChar(2);
		//CharacterTexture.getInstance().printString("helloworld");

	}

	public static GraphicEngine getGraphicEngine() {
		if (graphicEngine == null) {
			graphicEngine = new GraphicEngine();
		}
		return graphicEngine;
	}

	public static void render() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		viewMain.initRendering();
		
		
		  //GL11.glEnable(GL11.GL_FOG);
		  //GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR); 
		  //GL11.glFogf(GL11.GL_FOG_START, 48.f);
		  //GL11.glFogf(GL11.GL_FOG_END, 256.f);
		
		double t1 = System.currentTimeMillis();
		innerRender();

		double t2 = System.currentTimeMillis()-t1;
		Profiler.getInstance().push("render", t2,Timer.getCurrT());

		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		hud.draw();

		MenuSelectBlock.getInstance().draw();
		MenuSelectColorMap.getInstance().draw();
		ColorMap.getInstance().plotLegend(ColorMap.getInstance().cm);
		Console.getInstance().draw();


		// test window view : 
		View viewWindowCoord = ViewBuilder.viewWindowCoord();	
		viewWindowCoord.initRendering();

		//Profiler.getInstance().push("test", Timer.getDt(),Timer.getCurrT());
		//Profiler.getInstance().push("test2", Timer.getDt(),Timer.getCurrT());

		Profiler.getInstance().display();
		// GL11.glFlush();
		Display.update();

	}

	public static void innerRender() {
		// GL11.glFlush();
		// Grid.draw();
		// GL11.glFlush();

		Octree octree = new Octree(0, 0, 0, 0);
		octree.setContent(8);
		OctreeRendererLines.render(octree);

		// gameObjectRenderer.render(GameEngine.getPlayer());
		// light.setPositionSunLight();


		BlockRendererLines.getInstance().renderEmphasize(ModifAdder.getNodeToRemove(), 0);
		BlockRendererLines.getInstance().renderEmphasize(ModifAdder.getNodeToAdd(), 1);




		OctreeRendererLines.render(GameEngine.getOctree());
		if (GameEngine.getOctreeBuilder() instanceof OctreeBuilderWorldFuntionCullerModif){
			ModifOctree modif = ((OctreeBuilderWorldFuntionCullerModif) GameEngine.getOctreeBuilder()).getModifOctree();
			OctreeRendererLines.renderModif(modif);
		}

		light.initLight();

		vboFace.draw();

		Light.disableLights();

		light.draw();


		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	public static void update() {
		double t1 = System.currentTimeMillis();
		vboFace.update();
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
