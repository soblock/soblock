package org.wavecraft.ui.menu.twl;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

public class MainMenu extends Widget{
	
	private Button buttonNewGame;
	private Button buttonLoadGame;
	private Button buttonOptions;
	private Button buttonQuit;
	
	private String pathToTheme = "mainmenu.xml";
	
	
	
	public MainMenu(){
		createButton();
		layout();
	}



	public String getPathToTheme() {
		return pathToTheme;
	}
	

	private void createButton() {
		buttonNewGame = new Button("New Game");
		
		buttonLoadGame = new Button("Load Game");
		buttonOptions = new Button("Options");
		buttonQuit =  new Button("Quit");
		
		buttonNewGame.setTheme("button");
		buttonLoadGame.setTheme("button");
		buttonOptions.setTheme("button");
		buttonQuit.setTheme("button");
		
		add(buttonNewGame);
		add(buttonLoadGame);
		add(buttonOptions);
		add(buttonQuit);
	}

	protected void layout(){
		int left = 350;
		int top = 100;
		int dy = 50;
		
		buttonNewGame.setPosition(left, top);
		buttonNewGame.setSize(150, 33);
		
		buttonLoadGame.setPosition(left, top + dy);
		buttonLoadGame.setSize(150, 33);

		buttonOptions.setPosition(left, top + 2*dy);
		buttonOptions.setSize(150, 33);
		
		buttonQuit.setPosition(left, top + 3*dy);
		buttonQuit.setSize(150, 33);
		
		//button.adjustSize(); //Calculate optimal size instead of manually setting it
	}

}
