package org.wavecraft.ui.events;

import org.wavecraft.ui.KeyboardBinding;

public class UiEventKeyboardDown implements UiEvent{
	public KeyboardBinding key;

	public UiEventKeyboardDown(KeyboardBinding key){
		this.key = key;
	}
}
