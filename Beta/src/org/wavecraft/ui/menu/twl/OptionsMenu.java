package org.wavecraft.ui.menu.twl;

import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMenu;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;

public class OptionsMenu extends Widget implements ResizableWidget {

	private Button buttonGraphics;
	private Button buttonControlls;
	private Button buttonBackToMain;
	
	private static OptionsMenu instance;
	public static OptionsMenu getInstance(){
		if (instance == null){
			instance = new OptionsMenu();
		}
		return instance;
	}
	
	private OptionsMenu(){
		initButtons();
	}
	
	private void initButtons(){
		buttonGraphics = new Button("Graphics");
		buttonControlls = new Button("Controlls");
		buttonBackToMain = new Button("Back to Main Menu");
		
		buttonGraphics.setTheme("button");
		buttonControlls.setTheme("button");
		buttonBackToMain.setTheme("button");
		
		buttonBackToMain.addCallback(new Runnable() {
			
			@Override
			public void run() {
				UiEventMediator.getUiEventMediator().add(UiEventMenu.NAV_MENU_MAIN);
			}
		});
		

		add(buttonGraphics);
		add(buttonControlls);
		add(buttonBackToMain);
	}
	
	@Override
	public void resize(int w, int h) {
		int buttonW = 150;
		int buttonH= 33;
		int left = w/2 - buttonW/2;
		int top = Math.max(40, h/2 - 5 * buttonH);
		int dy = 50;
		
		buttonGraphics.setPosition(left, top);
		buttonGraphics.setSize(buttonW, buttonH);
		
		buttonControlls.setPosition(left, top + dy);
		buttonControlls.setSize(buttonW, buttonH);
		
		buttonBackToMain.setPosition(left, top + 2*dy);
		buttonBackToMain.setSize(buttonW, buttonH);
		
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public String getPathToThemeFile() {
		return "optionsmenu.xml";
	}

}
