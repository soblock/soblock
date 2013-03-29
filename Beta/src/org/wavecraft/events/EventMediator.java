package org.wavecraft.events;

import java.util.ArrayList;




// templated class for event handling
public class EventMediator<Event,EventListenerSub extends EventListener<Event>> {

	// the listeners is a set so that we can remove an element
	protected  ArrayList<EventListenerSub> listeners;
	// double buffer listeners
	protected ArrayList<EventListenerSub> nextListeners;
	// double buffer events to prevent concurrent access
	protected ArrayList<Event> nextEvents;
	protected ArrayList<Event> events;


	protected EventMediator(){
		this.listeners = new ArrayList<EventListenerSub>();
		this.events = new ArrayList<Event>();
		this.nextEvents = new ArrayList<Event>();
		this.nextListeners = new ArrayList<EventListenerSub>();

	}

	public void add(EventListenerSub listener){
		nextListeners.add(listener);
	}
	
	public void remove(EventListenerSub listener){
		listeners.remove(listener);
	}

	public void add(Event event){
		nextEvents.add(event);
	}

	public void notifyMyListeners(){
		events = nextEvents;
		nextEvents = new ArrayList<Event>();
		listeners.addAll(nextListeners);
		nextListeners.clear();
		for (Event event : events){
			for (EventListenerSub listener : listeners){
				listener.handle(event);
			}
		}
		events.clear();
		
	}

	public void printAllListenersType(){
		for (EventListenerSub listener : listeners){
			System.out.println(listener.getClass().toString());
		}
	}
}
