package org.wavecraft.geometry.blocktree;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

public class BlocktreePriorityPosition implements BlocktreePriority, UiEventListener {

	private Coord3d position;
	private double radius=6;
	private static final double RADIUS_MIN = 1;
	private static final double RADIUS_MAX = 512;

	public BlocktreePriorityPosition(){
		UiEventMediator.addListener(this);
	}
	
	@Override
	public double priority(Blocktree block) {
		if (block.getJ()<=Blocktree.BLOCK_LOG_SIZE +1) {return 0;}

		switch (block.getState()) {
		case GRAND_FATHER:
			return ratioTtjOverDistance(block);
		case PATRIARCH:
			return 1/ratioTtjOverDistance(block);
		default:
			return 0;
		}
	}

	private double ratioTtjOverDistance(DyadicBlock block){
		return radius*block.edgeLentgh()/(block.distance(position)+0.01);
	}
	
	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){
			UiEventKeyboardPressed ekp = (UiEventKeyboardPressed) e;
			switch (ekp.key) {
			case KEYBOARD_CULLING_INCREASE:
				System.out.println("Blocktree cull"+radius);
				radius++;
				break;

			case KEYBOARD_CULLING_DECREASE:
				System.out.println("Blocktree cull"+radius);
				radius--;
				break;
				
			default:
				break;
			}
			radius = Math.min(Math.max(radius, RADIUS_MIN), RADIUS_MAX);
		}
	}
	
	public void setPosition(Coord3d position) {
		this.position = position;
	}


}
