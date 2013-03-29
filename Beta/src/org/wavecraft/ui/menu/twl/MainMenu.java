package org.wavecraft.ui.menu.twl;

import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMenu;


import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;

public class MainMenu extends Widget implements ResizableWidget{
	
	private Button buttonNewGame;
	private Button buttonLoadGame;
	private Button buttonOptions;
	private Button buttonQuit;
	
	private final String pathToTheme = "mainmenu.xml";
	
	private static MainMenu instance;
	
	public static MainMenu getInstance(){
		if (instance == null){
			instance = new MainMenu();
		}
		return instance;
	}
	
	private MainMenu(){
		initButton();
	}


	private void initButton() {
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
				UiEventMediator.getUiEventMediator().add(UiEventMenu.START_NEW_GAME);
			}
		});
		
		buttonQuit.addCallback(new Runnable() {
			@Override
			public void run() {
				UiEventMediator.getUiEventMediator().add(UiEventMenu.QUIT);
			}
		});
		
		buttonOptions.addCallback(new Runnable() {
			@Override
			public void run() { 
				UiEventMediator.getUiEventMediator().add(UiEventMenu.NAV_MENU_OPTIONS);
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
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public String getPathToThemeFile() {
		return pathToTheme;
	}

}
