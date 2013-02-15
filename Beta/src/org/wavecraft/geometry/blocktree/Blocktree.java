package org.wavecraft.geometry.blocktree;



import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.DyadicBlock;


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
	
	
	
	
	/**
	 * returns a recursive copy of the blocktree and its sons.
	 * use it to safely modify a tree inside a separate thread 
	 */
	public Blocktree cloneRecursively(){
		Blocktree safeThis = new Blocktree(this.x, this.y, this.z, this.getJ());
		safeThis.father = this.father;
		safeThis.content = content;
		safeThis.state = state;
		if (this.hasSons()){
			safeThis.sons = new Blocktree[8];
			for (int offset = 0; offset<8; offset++){
				safeThis.sons[offset] = this.sons[offset].cloneRecursively();
			}
		}
		return safeThis;
	}
	
	/**
	 * replaces this with the provided safeThis in my father
	 * @param safeThis
	 * a safe copy of this (typically obtained with cloneRecusively)
	 */
	public void replaceMeWtih(Blocktree safeThis){
		if (father!=null){
			father.sons[this.offset()] = safeThis;
		}
		else {
			try {
				throw new  Exception("should never happend");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String toString() {
		return "Blocktree"+ super.toString() +"[state=" + state + ", sons=" 
				+ ", content=" + content + "]";
	}

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
			sons[offset] = new Blocktree(this.subBlock(offset), this);
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
