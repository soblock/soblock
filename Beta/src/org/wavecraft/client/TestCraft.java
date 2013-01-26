package org.wavecraft.client;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.graphics.GraphicEngine;
import org.wavecraft.stats.StatisticsClient;
import org.wavecraft.ui.UserInterface;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;

public class TestCraft  implements UiEventListener {

	private static boolean quitNotification = false;

	public static void main(String[] args) {
		WaveCraftSP wsp = new WaveCraftSP();
		wsp.init();		
		while(!(quitNotification)){
			wsp.logic();
			wsp.render();
		}
		wsp.close();
	}

	protected void init() {
		UserInterface.getUserInterface();
		GameEngine.getGameEngine();
		GraphicEngine.getGraphicEngine();

		ClientControllerSP.getClientControllerSP();

		Timer.getTimer();
		StatisticsClient.getStatisticsClient();

		// bind stuff
		ClientControllerSP.bindStuff();
		// add listeners
		UserInterface.addUiEventListener(this);
	}

	protected void logic(){
		UserInterface.getEventAndSendNotification();
		Timer.update();
		StatisticsClient.update();
		GameEngine.update();
		GraphicEngine.update();
	}

	protected void render() {	

		GraphicEngine.render();




	}

	protected void close() {
		UserInterface.close();
	}

	@Override
	public void handle(UiEvent event) {
		if (event instanceof UiEventKeyboardPressed){
			UiEventKeyboardPressed eventKeyboard = (UiEventKeyboardPressed) (event);
			switch (eventKeyboard.key) {
			case KEYBOARD_APP_CLOSE:
				quitNotification = true;
				System.out.println("quiting application");
				break;
			default:
				break;
			}
		}
	}


}

