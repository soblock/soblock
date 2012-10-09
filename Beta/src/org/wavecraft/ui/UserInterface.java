package org.wavecraft.ui;


import org.wavecraft.graphics.renderer.octree.ColorMap;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.menu.MenuSelectColorMap;
import org.wavecraft.ui.menu.MenuSelectBlock;


// user interface main class
// singleton 
public class UserInterface {
	
	private static UserInterface ui = null;
	
	private static WindowFrame window;
	private static KeyBoard keyboard;
	private static Mouse mouse;
	
	public static UserInterface getUserInterface(){
		if (ui == null){
			ui = new UserInterface();
		}
		return ui;
	}
	
	private UserInterface(){
		UiEventMediator.getUiEventMediator();
		window= new WindowFrame("WaveCraft Beta");
		keyboard=new KeyBoard();
		mouse = Mouse.getInstance();
		MenuSelectBlock.getInstance();
		MenuSelectColorMap.getInstance();
		ColorMap.getInstance();
		// register listener to mediator
		UiEventMediator.addListener(window);
	}
	
	public static void getEventAndSendNotification(){
		keyboard.getKeyboardEvents();
		mouse.getMouseEvent();
		UiEventMediator.notifyAllListeners();
	}
	
	
	
	public static void close(){
		window.close();
	}
	
	public static void addUiEventListener(UiEventListener listener){
		UiEventMediator.addListener(listener);
	}
}
