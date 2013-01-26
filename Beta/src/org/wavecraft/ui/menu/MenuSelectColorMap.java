package org.wavecraft.ui.menu;

import java.util.HashSet;
import java.util.Set;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.Coord2d;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.builder.OctreeBuilderWorldFuntionCullerModif;
import org.wavecraft.geometry.worldfunction.ThreeDimContent;
import org.wavecraft.geometry.worldfunction.ThreeDimContentBiome;
import org.wavecraft.geometry.worldfunction.ThreeDimFunction;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionFlat;
import org.wavecraft.geometry.worldfunction.WorldFunction;
import org.wavecraft.geometry.worldfunction.WorldFunctionWrapper;
import org.wavecraft.graphics.renderer.octree.BlockColorerLines;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;


public class MenuSelectColorMap  {


	private static MenuSelectColorMap instance = null;
	
	
	Set<WCButton> buttons ;
	View view;
	
	public static MenuSelectColorMap getInstance(){
		if (instance==null){
			instance = new MenuSelectColorMap();
		}
		return instance;
	}
	
	private MenuSelectColorMap(){
		buttons = new HashSet<WCButton>();
		int nButtons = 4;
		double ymr = -0.5;
		double yMr = 0.5;
		
		double deltax = (yMr - ymr)/nButtons;
		double buttonSizex = deltax - 0.02;
		double buttonAspect = 3/4.0f;
		double buttonSizey = buttonSizex*buttonAspect;
		
		
		for (int i = 0;i<nButtons;i++){
			String buttonName = "";
			WCAction action = null;
			switch (i) {
			case 0:
				buttonName = "level set";
				class Action0 implements WCAction{
					@Override
					public void process() {
						OctreeBuilder builder = GameEngine.getOctreeBuilder(); 
						if (builder instanceof OctreeBuilderWorldFuntionCullerModif){
							WorldFunction wf = ((OctreeBuilderWorldFuntionCullerModif) builder).getWorldFunction();
							if (wf instanceof WorldFunctionWrapper){
								ThreeDimFunction functionForColormap = ((WorldFunctionWrapper) wf).getThreeDimFunction();
								double vminForColormap = -64;
								double vmaxForColormap = 64;
								BlockColorerLines.getInstance().setFunForColorMap(functionForColormap, vminForColormap, vmaxForColormap);
							}
						}
					}
				}
				action = new Action0();
				
				break;
			case 1:
				class Action1 implements WCAction{
					@Override
					public void process() {
						OctreeBuilder builder = GameEngine.getOctreeBuilder(); 
						if (builder instanceof OctreeBuilderWorldFuntionCullerModif){
							WorldFunction wf = ((OctreeBuilderWorldFuntionCullerModif) builder).getWorldFunction();
							if (wf instanceof WorldFunctionWrapper){
								
								
								ThreeDimContent content = ((WorldFunctionWrapper) wf).getThreeDimContent();
								if (content instanceof ThreeDimContentBiome){
									ThreeDimFunction functionForColormap = ((ThreeDimContentBiome) content).humidity;
									double vminForColormap = -0.4;
									double vmaxForColormap = 0.4;
									BlockColorerLines.getInstance().setFunForColorMap(functionForColormap, vminForColormap, vmaxForColormap);
								}
								
							}
						}
					}
				}
				action = new Action1();
				buttonName = "humidity";
				break;
			case 2:
				class Action2 implements WCAction{
					@Override
					public void process() {
						OctreeBuilder builder = GameEngine.getOctreeBuilder(); 
						if (builder instanceof OctreeBuilderWorldFuntionCullerModif){
							WorldFunction wf = ((OctreeBuilderWorldFuntionCullerModif) builder).getWorldFunction();
							if (wf instanceof WorldFunctionWrapper){
								
								
								ThreeDimContent content = ((WorldFunctionWrapper) wf).getThreeDimContent();
								if (content instanceof ThreeDimContentBiome){
									ThreeDimFunction functionForColormap = ((ThreeDimContentBiome) content).temperature;
									double vminForColormap = -0.4;
									double vmaxForColormap = 0.4;
									BlockColorerLines.getInstance().setFunForColorMap(functionForColormap, vminForColormap, vmaxForColormap);
								}
								
							}
						}
					}
				}
				action = new Action2();
				buttonName = "temperature";
				break;
			case 3:
				class Action3 implements WCAction{
					@Override
					public void process() {
						ThreeDimFunction fun = new ThreeDimFunctionFlat(0);
						double vmin = 0;
						double vmax = Math.pow(2, Octree.JMAX);
						BlockColorerLines.getInstance().setFunForColorMap(fun, vmin, vmax);
					}
				}
				action = new Action3();
				buttonName = "altitude";
				break;
			default:
				break;
			}

			WCButton button = new WCButton(buttonName,new Coord2d( 0.7 , ymr + i*deltax),
					new Coord2d(buttonSizex, buttonSizey), action);
			buttons.add(button);
			
		}
		
		view = ViewBuilder.viewMenu();
	}
	
	public void draw(){
		view.initRendering();
		for (WCButton button : buttons){
			button.draw();
		}
		
	}
}
