package org.wavecraft.ui.menu.twl;


import java.io.IOException;

import org.wavecraft.gameobjet.save.GameSaveManager;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMenu;


import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.BoxLayout.Direction;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;

public class MainMenu extends Widget implements ResizableWidget{

	private Button buttonNewGame = new Button("New Game");
	private Button buttonResumeGame = new Button("Resume Game");
	private Button buttonSaveGame = new Button("Save Game");
	private Button buttonLoadGame = new Button("Load Game");
	private Button buttonOptions = new Button("Options");
	private Button buttonQuit =  new Button("Quit");

	private int buttonW = 150;
	private int buttonH= 33;
	private int buttonSpacing = 40;

	private BoxLayout boxLayout = new BoxLayout(Direction.VERTICAL);

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
		buttonNewGame.addCallback(new Runnable() {
			@Override
			public void run() {
				UiEventMediator.getUiEventMediator().add(UiEventMenu.START_NEW_GAME);
				boxLayout.removeAllChildren();
				
				boxLayout.add(buttonResumeGame);
				boxLayout.add(buttonSaveGame);
				boxLayout.add(buttonNewGame);
				boxLayout.add(buttonLoadGame);
				boxLayout.add(buttonOptions);
				boxLayout.add(buttonQuit);
			}
		});

		buttonSaveGame.addCallback(new Runnable() {
			@Override
			public void run() {
				try {
					GameSaveManager.getInstance().writeSaveInFile("wavecraftmap1.wcm");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		buttonLoadGame.addCallback(new Runnable() {
			@Override
			public void run() {
				try {
					GameSaveManager.getInstance().loadAnApply("wavecraftmap1.wcm");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				UiEventMediator.getUiEventMediator().add(UiEventMenu.START_NEW_GAME);
			}
		});
		
		buttonResumeGame.addCallback(new Runnable() {
			@Override
			public void run() {
				UiEventMediator.getUiEventMediator().add(UiEventMenu.RESUME_GAME);
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

		boxLayout.add(buttonNewGame);
		boxLayout.add(buttonLoadGame);
		boxLayout.add(buttonOptions);
		boxLayout.add(buttonQuit);

		add(boxLayout);
	}

	public void resize(int w, int h){
		int left = w/2 - buttonW/2;
		int top = Math.max(40, h/2 - 5 * buttonH);

		buttonNewGame.setSize(buttonW, buttonH);
		buttonSaveGame.setSize(buttonW, buttonH);
		buttonResumeGame.setSize(buttonW, buttonH);
		buttonLoadGame.setSize(buttonW, buttonH);
		buttonOptions.setSize(buttonW, buttonH);
		buttonQuit.setSize(buttonW, buttonH);
		
		boxLayout.setSize(w, h);
		boxLayout.setSpacing(buttonSpacing);
		boxLayout.setPosition(left, top);
		boxLayout.setAlignment(Alignment.TOPLEFT);

	}

	@Override
	public Widget asWidget() {
		return this;
	}


}
