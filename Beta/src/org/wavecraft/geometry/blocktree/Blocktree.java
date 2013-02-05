package org.wavecraft.geometry.blocktree;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;

@SuppressWarnings("serial")
public class Blocktree extends DyadicBlock{

	public enum State{
		PATRIARCH,
		GRAND_FATHER,
		FATHER,
		DEAD_GROUND,
		DEAD_AIR,
		LEAF;
	}
	
	private State state;
	private Blocktree[] sons;
	private Blocktree father;
	private int content;
	
	
	
	
	public Blocktree(int x, int y, int z, int J) {
		super(x, y, z, J);
		this.state = State.DEAD_GROUND;
	}


	public Blocktree(DyadicBlock subBlock, Blocktree father) {
		super(subBlock.x, subBlock.y, subBlock.z, subBlock.getJ());
		this.father = father;
	}


	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Blocktree[] getSons() {
		return sons;
	}

	public void setContent(int content){
		this.content = content;
	}
	
	public void initSons(){
		sons = new Blocktree[8];
		for (int offset = 0; offset<8; offset++){
			sons[offset] = new Blocktree(this.subBlock(offset),this);
			sons[offset].setContent(content);
		}
	}

	public boolean hasSons(){
		return sons!=null;
	}
	
	public void killSons(){
		this.sons = null;
	}
	
}
