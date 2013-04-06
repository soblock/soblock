package org.wavecraft.ui.events;


import org.wavecraft.events.EventMediator;

// use patterng singleton to ensure that only one instance of a class is created
public class UiEventMediator extends EventMediator<UiEvent, UiEventListener>{
	private static UiEventMediator uiEventMediator=null;
	private UiEventMediator(){
		super();
	}
	
	public static UiEventMediator getUiEventMediator(){
		if (uiEventMediator == null){
			uiEventMediator = new UiEventMediator();
		}
		return uiEventMediator;
	}
	
	public void removeListener(UiEventListener listener){
		uiEventMediator.remove(listener);
	}
	
	public void addListener(UiEventListener listener){
		uiEventMediator.add(listener);
	}
	
	public  void addEvent(UiEvent event){
		uiEventMediator.add(event);
	}
	
	public  void notifyAllListeners(){
		uiEventMediator.notifyMyListeners();
	}
	
}
