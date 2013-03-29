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
				//String path =widget.getPathToThemeFile();
				String path = "mainmenu.xml";	
				if (themeManager==null){
					themeManager = ThemeManager.createThemeManager(MainMenu.getInstance().getClass().getResource(path), renderer);
				}
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
		if (widget!=null){
			widget.resize(WindowSize.getInstance().getW(), WindowSize.getInstance().getH());
		}
	}

	public void display(){
		if (gui!=null){
			gui.update();
		}
	}

	public ResizableWidget getWidget() {
		return widget;
	}

	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){
			UiEventKeyboardPressed ePressed = (UiEventKeyboardPressed) e;
			if (ePressed.key == KeyboardBinding.KEYBOARD_MENU){
				widget = MainMenu.getInstance();
				initGuiWithWidget();
			}
		}
		
		if (e instanceof UiEventMenu){
			UiEventMenu eMenu = (UiEventMenu) e;
			switch (eMenu) {
			case START_NEW_GAME:
				widget = null;
				gui = null;
				break;

			case NAV_MENU_OPTIONS:
				widget = OptionsMenu.getInstance();
				initGuiWithWidget();
				break;
				
			case NAV_MENU_MAIN:
				widget = MainMenu.getInstance();
				initGuiWithWidget();
				break;
				
			default:
				break;
			}
		}
	}

}
