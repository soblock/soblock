package org.wavecraft.ui.menu;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wavecraft.geometry.Coord2d;
import org.wavecraft.geometry.blocktree.Terran;
import org.wavecraft.graphics.view.View;
import org.wavecraft.graphics.view.ViewBuilder;


public class MenuSelectBlock {
	
	private static MenuSelectBlock instance = null;
	
	private List<Terran> selectableTerran = Arrays.asList(new Terran[]{
			Terran.MAN_BRICK,
			Terran.MAN_PARQUET,
			Terran.MAN_METAL,
			Terran.NAT_DIRT,
			Terran.NAT_GRASS,
			Terran.NAT_STONE
	});
	
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
		int nButtons = selectableTerran.size();
		double xmr = -0.5;
		double xMr = 0.5;
		
		double deltax = (xMr - xmr)/nButtons;
		double buttonSizex = deltax - 0.02;
		double buttonAspect = 3/4.0f;
		double buttonSizey = buttonSizex*buttonAspect;
		
		for (int i = 0;i<selectableTerran.size();i++){
			
			Coord2d positionRelative = new Coord2d(xmr+ (i-1)*deltax, -0.9);
			Coord2d sizeRelative = new Coord2d(buttonSizex, buttonSizey);
			
			WCButtonSelectBlock button = new WCButtonSelectBlock(selectableTerran.get(i),i+1, positionRelative, sizeRelative);
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
