package org.wavecraft.geometry.blocktree;

import java.util.ArrayList;
import java.util.List;

import org.wavecraft.geometry.DyadicBlock;

@SuppressWarnings("serial")
public class Blocktree extends DyadicBlock{
//
//	@Override
//	public int hashCode() {
//		return super.hashCode();
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		return super.equals(obj);
//	}

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




	public int getContent() {
		return content;
	}

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
				safeThis.sons[offset].setFather(safeThis);
			}
		}
		return safeThis;
	}

	/**
	 * replaces this with the provided illegitimateChild in my father.
	 * To be used on a copy of a node, typically obtained with cloneRecusively
	 * and updated in a separate thread
	 */
	public void becomeSonOfMyFather(){
		if (father!=null){
			father.sons[this.offset()] = this;
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

	public void setFather(Blocktree father) {
		this.father = father;
	}

	public Blocktree smallestCellContaining(DyadicBlock block) {
		if (this.equals(block))
			return this;
		else {
			if (this.contains(block)){
				if (hasSons()){
					int offset = this.findSonContaining(block);
					return sons[offset].smallestCellContaining(block);
				}
				else {
					return this;
				}
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Blocktree other = (Blocktree) obj;
		return (x==other.x && y==other.y && z==other.z && getJ()==other.getJ()); 
	}
	
	/**
	 * 
	 * @param state
	 */
	public List<Blocktree> listOfSonsOfStateGrandFather(){
		List<Blocktree> allSons = new ArrayList<Blocktree>();
		this.listOfSonsOfStateGrandFatherInner(allSons);
		return allSons;
	}

	private void listOfSonsOfStateGrandFatherInner(List<Blocktree> allSons) {
		if (this.state == State.GRAND_FATHER){
			allSons.add(this);
		}
		if (this.state == State.GRAND_FATHER || this.state == State.PATRIARCH){
			for (Blocktree son : this.getSons()){
				son.listOfSonsOfStateGrandFatherInner(allSons);
			}
		}
	}


	

}
