package org.wavecraft.ui.menu.twl;

import java.io.IOException;

import org.lwjgl.LWJGLException;

import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

public class MainTheme {

	private static MainTheme instance;
	private ThemeManager themeManager;
		
	
	private MainTheme() {
		LWJGLRenderer renderer;
		try {
			renderer = new LWJGLRenderer();
			String path = "maintheme.xml";
			try {
				themeManager =  ThemeManager.createThemeManager(this.getClass().getResource(path), renderer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (LWJGLException e1) {
			e1.printStackTrace();
		}
		
	}
	public static MainTheme getInstance(){
		if (instance == null){
			instance = new MainTheme();
		}
		return instance;
	}
	public ThemeManager getThemeManager() {
		return themeManager;
	}
	
	
}
