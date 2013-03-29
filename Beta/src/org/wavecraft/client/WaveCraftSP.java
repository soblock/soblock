package org.wavecraft.client;


import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.graphics.GraphicEngine;



import org.wavecraft.stats.StatisticsClient;
import org.wavecraft.ui.UserInterface;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMenu;

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
		//WaveCraftSP wsp = new WaveCraftBlockTreeSP();
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
		GameEngine.getGameEngine().prepareForExit();
		UserInterface.close();
	}

	@Override
	public void handle(UiEvent event) {
		if (event instanceof UiEventMenu){
			UiEventMenu eMenu = (UiEventMenu) event;
			if (eMenu == UiEventMenu.QUIT){
				quit();
			}
		}
	}
	
	public void quit(){
		//Date date = new Date();
		//String dateStr = DateFormat.getDateInstance().format(date);
		//Profiler.getInstance().writeLogInFile("wavecraft_profiler_output"+dateStr);
		quitNotification = true;
		System.out.println("quiting application");
	}


}
