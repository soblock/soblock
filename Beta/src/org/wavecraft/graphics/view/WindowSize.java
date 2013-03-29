package org.wavecraft.graphics.view;

import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventWindowResized;

//store up-to-date size of the window
public class WindowSize implements UiEventListener{
	private int w,h;
	private static WindowSize instance;


	public static WindowSize getInstance(){
		if (instance == null){
			instance = new WindowSize();
		}
		return instance;
	}

	private WindowSize(){
		w = 0;
		h = 0;
		UiEventMediator.getUiEventMediator().addListener(this);
	}

	@Override
	public void handle(UiEvent e) {
		// TODO Auto-generated method stub
		if (e instanceof UiEventWindowResized){
			w = ((UiEventWindowResized) e).w;
			h = ((UiEventWindowResized) e).h;
		}
	}
	
	public int getW(){
		return w;
	}
	
	public int getH(){
		return h;
	}
}
