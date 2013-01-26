package org.wavecraft.client;

import java.text.DateFormat;
import java.util.Date;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.graphics.GraphicEngine;
import org.wavecraft.modif.ModifAdder;

import org.wavecraft.stats.Profiler;
import org.wavecraft.stats.StatisticsClient;
import org.wavecraft.ui.UserInterface;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;

/**
 * Client for wavecraft single player mode
 * The application is designed according to the
 * Model View Controller pattern
 * 
 * The Model is GameEngine
 * The View is GraphicEngine
 * The Controller is the logicEngine
 * 
 * 
 * 
 * @author lolosifre
 * 
 */
public class WaveCraftSP implements UiEventListener {

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
		
		UserInterface.addUiEventListener(ModifAdder.getInstance());
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
				Date date = new Date();
				String dateStr = DateFormat.getDateInstance().format(date);
				Profiler.getInstance().writeLogInFile("wavecraft_profiler_output"+dateStr);
				quitNotification = true;
				System.out.println("quiting application");
				break;
			default:
				break;
			}
		}
	}


}
