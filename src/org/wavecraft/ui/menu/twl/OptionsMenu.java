package org.wavecraft.ui.menu.twl;

import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMenu;

import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.BoxLayout.Direction;

public class OptionsMenu extends Widget implements ResizableWidget {

	private Button buttonGraphics = new Button("Graphics");;
	private Button buttonControlls = new Button("Controlls");
	private Button buttonBackToMain = new Button("Back to Main Menu");
	
	private int buttonW = 150;
	private int buttonH= 33;
	private int buttonSpacing = 40;

	private BoxLayout boxLayout = new BoxLayout(Direction.VERTICAL);
	
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
		
		buttonBackToMain.addCallback(new Runnable() {
			@Override
			public void run() {
				UiEventMediator.getUiEventMediator().add(UiEventMenu.NAV_MENU_MAIN);
			}
		});

		boxLayout.add(buttonGraphics);
		boxLayout.add(buttonControlls);
		boxLayout.add(buttonBackToMain);
		add(boxLayout);
	}
	
	public void resize(int w, int h){
		int left = w/2 - buttonW/2;
		int top = Math.max(40, h/2 - 5 * buttonH);

		buttonGraphics.setSize(buttonW, buttonH);
		buttonControlls.setSize(buttonW, buttonH);
		buttonBackToMain.setSize(buttonW, buttonH);
		
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
