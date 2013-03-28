package org.wavecraft.ui.menu.twl;


import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.gameobject.physics.PhysicsWrapper;
import org.wavecraft.graphics.view.WindowSize;
import org.wavecraft.ui.Mouse;
import org.wavecraft.ui.Mouse.State;
import org.wavecraft.ui.menu.twl.MenuController.Menu;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;

public class MainMenu extends Widget implements ResizableWidget{
	
	private Button buttonNewGame;
	private Button buttonLoadGame;
	private Button buttonOptions;
	private Button buttonQuit;
	
	private MenuController menuController;
	
	private String pathToTheme = "mainmenu.xml";
	
	private static MainMenu instance;
	
	public static MainMenu getInstance(MenuController menuController){
		if (instance == null){
			instance = new MainMenu(menuController);
		}
		return instance;
	}
	
	private MainMenu(MenuController menuController){
		this.menuController = menuController;
		createButton();
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
		
		buttonNewGame.addCallback(new Runnable() {
			
			@Override
			public void run() {
				Mouse.getInstance().setState(State.ACTIVE);
				menuController.setMenu(Menu.DISABLE);
				GameEngine.startNewGame();
				org.lwjgl.input.Mouse.setGrabbed(true);
			}
		});
		
		add(buttonNewGame);
		add(buttonLoadGame);
		add(buttonOptions);
		add(buttonQuit);
	}

	public void resize(int w, int h){
		int buttonW = 150;
		int buttonH= 33;
		int left = w/2 - buttonW/2;
		int top = Math.max(40, h/2 - 5 * buttonH);
		int dy = 50;
		
		
		buttonNewGame.setPosition(left, top);
		buttonNewGame.setSize(buttonW, buttonH);
		
		buttonLoadGame.setPosition(left, top + dy);
		buttonLoadGame.setSize(buttonW, buttonH);

		buttonOptions.setPosition(left, top + 2*dy);
		buttonOptions.setSize(buttonW, buttonH);
		
		buttonQuit.setPosition(left, top + 3*dy);
		buttonQuit.setSize(buttonW, buttonH);
		
		//button.adjustSize(); //Calculate optimal size instead of manually setting it
	}

	@Override
	public Widget asWidget() {
		return this;
	}


	

	

}
