package org.wavecraft.events;

import java.util.ArrayList;



import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.ui.events.UiEventWindowResized;

// templated class for event handling
public class EventMediator<Event,EventListenerSub extends EventListener<Event>> {

	// the listeners is a set so that we can remove an element
	protected  ArrayList<EventListenerSub> listeners;
	// double buffer events to prevent concurrent access
	protected ArrayList<Event> nextEvents;
	protected ArrayList<Event> events;
	protected int position;

	protected EventMediator(){
		this.listeners = new ArrayList<EventListenerSub>();
		this.events = new ArrayList<Event>();
		this.nextEvents = new ArrayList<Event>();
		this.position = 0;
	}

	public void add(EventListenerSub listener){
		listeners.add(listener);
	}
	
	public void remove(EventListenerSub listener){
		listeners.remove(listener);
	}

	public void add(Event event){
		nextEvents.add(event);
	}

	public void notifyMyListenersForOneEvent(){
		swap();
		if (events.size()>0) {
			if (events.get(position) instanceof OctreeEvent){
				//System.out.println(events.get(position).toString());

			}
			for (EventListenerSub listener : listeners){
				listener.handle(events.get(position));
			}
			position++;
		}
	}

	public boolean remainingEventsInCurrentBuffer(){
		return (position < events.size());
	}

	public void swap(){
		if (!remainingEventsInCurrentBuffer()){
			position =0;
			events = nextEvents;
			nextEvents = new ArrayList<Event>();
		}
	}

	public  void notifyMyListeners(){
		events = nextEvents;
		nextEvents = new ArrayList<Event>();
		for (Event event : events){
			if (events.get(position) instanceof OctreeEvent){
				//System.out.println(events.get(position).toString());
			}
			if (events.get(position) instanceof UiEventWindowResized){
				//System.out.println(events.get(position).toString());
			}
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
