package org.wavecraft.graphics.vbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.ui.KeyboardBinding;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

/**
 * singleton
 * this is a pool of vertex buffer object corresponding to blocktree
 * @author laurentsifre
 *
 */
public class VBOBlocktreePool implements UiEventListener{

	private enum State{
		DRAW_NOTHING,
		DRAW_ALL,
		DRAW_SMALL
	}

	private static VBOBlocktreePool instance;

	private State state = State.DRAW_ALL;

	private HashMap<Blocktree, VBOBlockTreeGrandFather> uploaded;
	private HashMap<Blocktree, VBOBlockTreeGrandFather> toUpload;
	private List<Blocktree> toUnload;

	public static VBOBlocktreePool getInstance(){
		if (instance == null){
			instance = new VBOBlocktreePool();
		}
		return instance;
	}

	private VBOBlocktreePool(){
		state = State.DRAW_ALL;
		UiEventMediator.getUiEventMediator().add(this);
		uploaded = new HashMap<Blocktree, VBOBlockTreeGrandFather>();
		toUpload = new HashMap<Blocktree, VBOBlockTreeGrandFather>();
		toUnload = new ArrayList<Blocktree>();
	}

	public void put(Blocktree blocktree, VBOBlockTreeGrandFather vbo){
		toUpload.put(blocktree, vbo);
	}

	public void prepareToUnload(Blocktree blocktree){
		toUnload.add(blocktree);
	}

	public void uploadAll(){
		for (Blocktree blocktree : toUpload.keySet()){
			VBOBlockTreeGrandFather vbo = toUpload.get(blocktree);
			vbo.uploadToGrahpicCard();
			uploaded.put(blocktree, vbo);
			//System.out.println("uPload "+blocktree);

		}
		toUpload.clear();
	}

	public void unloadAll(){
		for (Blocktree blocktree : toUnload){
			VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
			if (vbo != null){
				vbo.unloadFromGraphicCard();
				uploaded.remove(blocktree);
				//System.out.println("uNload "+blocktree);
			} 
			else {
				//System.out.println("did not retrieve" + blocktree);
			}
		}
		toUnload.clear();
	}

	public void render(){
		switch (state) {
		case DRAW_ALL:
			for (Blocktree blocktree : uploaded.keySet()){
				VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
				vbo.render();
			}
			break;

		case DRAW_SMALL:
			for (Blocktree blocktree : uploaded.keySet()){
				if (blocktree.getJ()<=Blocktree.BLOCK_LOG_SIZE+1){
					VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
					vbo.render();
				}
			}
			break;

		default:
			break;
		}

	}

	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){
			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_SWITCH_VBORAW){
				switch (state) {
				case DRAW_ALL:
					state = State.DRAW_SMALL;
					break;
					
				case DRAW_SMALL:
					state = State.DRAW_NOTHING;
					break;

				case DRAW_NOTHING:
					state = State.DRAW_ALL;
					break;
				default:
					break;
				}
			}

		}
	}

}
