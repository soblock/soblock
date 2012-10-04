package org.wavecraft.events;

public interface EventListener<Event> {
	public void handle(Event e);
}
