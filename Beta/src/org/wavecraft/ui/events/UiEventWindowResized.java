package org.wavecraft.ui.events;

public class UiEventWindowResized implements UiEvent {
	public int w, h;

	public UiEventWindowResized(int w, int h) {
		this.w = w;
		this.h = h;
	}

	@Override
	public String toString() {
		return "UiEventWindowResized [w=" + w + ", h=" + h + "]";
	}
}
