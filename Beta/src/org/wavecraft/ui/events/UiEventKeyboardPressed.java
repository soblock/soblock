package org.wavecraft.ui.events;

import org.wavecraft.ui.KeyboardBinding;

public class UiEventKeyboardPressed implements UiEvent {
	public KeyboardBinding key;
	
	public UiEventKeyboardPressed(KeyboardBinding key){
		this.key = key;
	}
	
}
