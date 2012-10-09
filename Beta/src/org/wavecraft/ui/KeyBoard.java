package org.wavecraft.ui;



import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardDown;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventMediator;



/**
 * keyboard events and status handler
 * @author lolosifre
 *
 */

public class KeyBoard {
	
	private HashMap<Integer, KeyboardBinding> bindingMap;

	public KeyBoard(){
	    setDefaultBindingMap();
		try {
			Keyboard.create();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setDefaultBindingMap(){
		this.bindingMap = new HashMap<Integer, KeyboardBinding>();
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_Q),KeyboardBinding.KEYBOARD_APP_CLOSE);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_EQUALS),KeyboardBinding.KEYBOARD_WINDOW_ENLARGE);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_MINUS),KeyboardBinding.KEYBOARD_WINDOW_REDUCE);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_W),KeyboardBinding.KEYBOARD_MOVE_FORWARD);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_S),KeyboardBinding.KEYBOARD_MOVE_BACKWARD);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_A),KeyboardBinding.KEYBOARD_MOVE_STRAFELEFT);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_D),KeyboardBinding.KEYBOARD_MOVE_STRAFERIGHT);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_SPACE),KeyboardBinding.KEYBOARD_MOVE_UP);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_C),KeyboardBinding.KEYBOARD_MOVE_DOWN);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_R),KeyboardBinding.KEYBOARD_SPEED_UP1);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_T),KeyboardBinding.KEYBOARD_SPEED_UP2);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_Y),KeyboardBinding.KEYBOARD_SPEED_UP3);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_U),KeyboardBinding.KEYBOARD_SPEED_UP4);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_O),KeyboardBinding.KEYBOARD_SWITCH_OCTREEDRAW);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_P),KeyboardBinding.KEYBOARD_SWITCH_COLOR);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_L),KeyboardBinding.KEYBOARD_CULLING_INCREASE);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_RETURN),KeyboardBinding.KEYBOARD_GAME_ADDBLOCK);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_BACK),KeyboardBinding.KEYBOARD_GAME_KILLBLOCK);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_Z),KeyboardBinding.KEYBOARD_MODIF_INC_ADD);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_X),KeyboardBinding.KEYBOARD_MODIF_DEC_ADD);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_F),KeyboardBinding.KEYBOARD_SWITCH_PHYSICS);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_0),KeyboardBinding.KEYBOARD_CONTENT_0);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_1),KeyboardBinding.KEYBOARD_CONTENT_1);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_2),KeyboardBinding.KEYBOARD_CONTENT_2);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_3),KeyboardBinding.KEYBOARD_CONTENT_3);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_4),KeyboardBinding.KEYBOARD_CONTENT_4);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_5),KeyboardBinding.KEYBOARD_CONTENT_5);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_6),KeyboardBinding.KEYBOARD_CONTENT_6);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_7),KeyboardBinding.KEYBOARD_CONTENT_7);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_8),KeyboardBinding.KEYBOARD_CONTENT_8);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_9),KeyboardBinding.KEYBOARD_CONTENT_9);
		this.bindingMap.put(Integer.valueOf(Keyboard.KEY_ESCAPE),KeyboardBinding.KEYBOARD_GRAB_MOUSE);
	}

	public void getKeyboardEvents(){
		getPressedEvents();
		getIsDownEvents();
	}
	
	private void getPressedEvents(){
		while (Keyboard.next()) {
			int kc = Keyboard.getEventKey() ;
				if (Keyboard.getEventKeyState()) {
					Integer ikc = Integer.valueOf(kc);
					if (this.bindingMap.containsKey(ikc)){
						KeyboardBinding keyboardBinding= this.bindingMap.get(ikc);
						UiEvent event = new UiEventKeyboardPressed(keyboardBinding);
						UiEventMediator.addEvent(event);
				}
			}
		}				
	}
	
	private void getIsDownEvents(){
		for (Entry<Integer, KeyboardBinding> keyVal : bindingMap.entrySet()){
			if (Keyboard.isKeyDown(keyVal.getKey())){
				KeyboardBinding keyboardBinding = keyVal.getValue();
				UiEvent event = new UiEventKeyboardDown(keyboardBinding);
				UiEventMediator.addEvent(event);
			}
		}
	}


}