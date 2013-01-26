package org.wavecraft.ui.menu;

import java.util.HashSet;
import java.util.Set;

import org.wavecraft.geometry.Coord2d;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;


public class MenuSelectBlock {
	
	private static MenuSelectBlock instance = null;
	
	
	Set<WCButton> buttons ;
	View view;
	
	public static MenuSelectBlock getInstance(){
		if (instance==null){
			instance = new MenuSelectBlock();
		}
		return instance;
	}
	
	private MenuSelectBlock(){
		buttons = new HashSet<WCButton>();
		int nButtons = 11;
		double xmr = -0.5;
		double xMr = 0.5;
		
		double deltax = (xMr - xmr)/nButtons;
		double buttonSizex = deltax - 0.02;
		double buttonAspect = 3/4.0f;
		double buttonSizey = buttonSizex*buttonAspect;
		
		for (int i = 1;i<nButtons;i++){
			
			Coord2d positionRelative = new Coord2d(xmr+ (i-1)*deltax, -0.9);
			Coord2d sizeRelative = new Coord2d(buttonSizex, buttonSizey);
			int buttonLabel = (i<10)?i:0;
			WCButtonSelectBlock button = new WCButtonSelectBlock(buttonLabel, positionRelative, sizeRelative);
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
