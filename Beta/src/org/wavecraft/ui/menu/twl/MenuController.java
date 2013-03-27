package org.wavecraft.ui.menu.twl;



import java.io.IOException;

import org.lwjgl.LWJGLException;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

public class MenuController {

	private GUI gui;
	private Widget widget;
	private ThemeManager themeManager;
	LWJGLRenderer renderer ; 

	public MenuController(){

		
		try {
			renderer =  new LWJGLRenderer();
			this.widget = new MainMenu();
			initGuiWithWidget();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

	}
	
	private void initGuiWithWidget(){
		this.gui = new GUI(widget, renderer);
		
		try {
			String path = ((MainMenu) widget).getPathToTheme();
			themeManager = ThemeManager.createThemeManager(widget.getClass().getResource(path), renderer);
		}
		catch (IOException e){
			e.printStackTrace();
		}
		gui.applyTheme(themeManager);

	}

	public void display(){
		if (gui!=null){
			gui.update();
		}
	}

}
