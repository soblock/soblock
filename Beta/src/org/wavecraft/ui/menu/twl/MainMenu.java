package org.wavecraft.ui.menu.twl;


import org.wavecraft.graphics.view.WindowSize;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;

public class MainMenu extends Widget implements ResizableWidget{
	
	private Button buttonNewGame;
	private Button buttonLoadGame;
	private Button buttonOptions;
	private Button buttonQuit;
	
	private String pathToTheme = "mainmenu.xml";
	
	
	
	public MainMenu(){
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
