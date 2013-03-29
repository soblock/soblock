package org.wavecraft.ui.menu.twl;



import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.wavecraft.graphics.view.WindowSize;
import org.wavecraft.ui.KeyboardBinding;
import org.wavecraft.ui.Mouse;
import org.wavecraft.ui.Mouse.State;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMenu;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

public class MenuController implements UiEventListener{

	private GUI gui;
	private ResizableWidget widget;
	public enum Menu {
		MAIN_MENU,
		IN_GAME_CLEAN,
		IN_GAME_DEBUG
	}

	private Map<ResizableWidget, GUI> allMenus = new HashMap<ResizableWidget, GUI>();


	private ThemeManager themeManager;
	LWJGLRenderer renderer ; 

	public MenuController(){

		UiEventMediator.getUiEventMediator().addListener(this);
		
		try {
			renderer =  new LWJGLRenderer();
			this.widget = MainMenu.getInstance();
			initGuiWithWidget();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		Mouse.getInstance().setState(State.NAV_MENU);

	}

	private void initGuiWithWidget(){

		// check gui is not yet instanciated
		if (allMenus.containsKey(widget)){
			this.gui = allMenus.get(widget);
		} // if not instantiate it
		else {
			try {
				String path = widget.getPathToThemeFile();
				themeManager = ThemeManager.createThemeManager(widget.getClass().getResource(path), renderer);
			}
			catch (IOException e){
				e.printStackTrace();
			}

			this.gui = new GUI(widget.asWidget(), renderer);
			gui.applyTheme(themeManager);
			allMenus.put(widget, gui);
		}

	}

	public void refreshDimension(){
		renderer.syncViewportSize();
		widget.resize(WindowSize.getInstance().getW(), WindowSize.getInstance().getH());
	}

	public void display(){
		if (gui!=null){
			gui.update();
		}
	}

	public ResizableWidget getWidget() {
		return widget;
	}

	public void setMenu(Menu menu) {
		switch (menu) {
		case IN_GAME_CLEAN:
			gui = null;
			Mouse.getInstance().setState(State.IN_GAME);
			break;

		case MAIN_MENU:
			widget = MainMenu.getInstance();
			initGuiWithWidget();
			Mouse.getInstance().setState(State.NAV_MENU);
			
		default:
			break;
		}
	}

	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){
			UiEventKeyboardPressed ePressed = (UiEventKeyboardPressed) e;
			if (ePressed.key == KeyboardBinding.KEYBOARD_MENU){
				setMenu(Menu.MAIN_MENU);
			}
		}
		
		if (e instanceof UiEventMenu){
			UiEventMenu eMenu = (UiEventMenu) e;
			switch (eMenu) {
			case START_NEW_GAME:
				setMenu(Menu.IN_GAME_CLEAN);
				break;

			default:
				break;
			}
		}
	}

}
