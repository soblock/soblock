package org.wavecraft.geometry.octree;

import java.util.ArrayList;

import org.wavecraft.Soboutils.Math_Soboutils;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.modif.ModifOctree;



@SuppressWarnings("serial")
// put every thing application-specific ( fluid, modif etc... ) in state
public class Octree extends DyadicBlock{
	public static int JMAX = 8	;
	protected Octree[] sons = null;
	protected Octree father = null;
	protected OctreeState state = null;
	protected int content = 0;


	public Octree(DyadicBlock block, Octree father){
		super(block.x,block.y,block.z,block.getJ());
		this.father = father;
		this.state = OctreeStateNotYetVisited.getInstance();
	}


	public Octree(int x,int y,int z ,int J){
		super(x,y,z,J);
		this.father = null;
		this.state = OctreeStateNotYetVisited.getInstance();
	}

	public OctreeState getState(){
		return state;
	}

	public void setState(OctreeState state){
		this.state = state;
	}

	public boolean hasSons(){
		return (sons!=null);
	}

	public void initSons(){
		sons = new Octree[8];
		for (int offset = 0; offset<8; offset++){
			sons[offset] = new Octree(this.subBlock(offset),this);
			sons[offset].setContent(content);
		}
	}

	public void initSonsQuietly(){
		sons = new Octree[8];
		for (int offset = 0; offset<8; offset++){
			sons[offset] = new Octree(this.subBlock(offset),this);
			sons[offset].setState(OctreeStateDead.getInstance()); // 30 august 12
		}
	}

	public void killSons(){
		if (this.sons!= null){
			for (int offset = 0 ; offset<8 ; offset++){
				OctreeEvent event = new OctreeEvent(sons[offset],OctreeEventKindof.KILL);
				OctreeEventMediator.addEvent(event);
			}
		}
		this.sons = null;
	}

	public void killSonsQuietly(){
		this.sons = null;
	}
	public Octree[] getSons(){
		return sons;
	}

	public Octree getFather(){
		return father;
	}

	public void setContent(int content){
		this.content = content;
	}

	public int getContent(){
		return content;
	}

	public Octree smallestCellContaining(DyadicBlock block) {
		if (this.getJ() == block.getJ())
			return this;
		else {
			if (hasSons()){
				int offset = findSonContaining(block);
				return sons[offset].smallestCellContaining(block);
			}
			else {
				return this;
			}
		}
	}

	public int findSonContaining(DyadicBlock block) {
		return Math_Soboutils.ithbit(block.x, this.getJ() - block.getJ()) + 2
		* Math_Soboutils.ithbit(block.y, this.getJ() - block.getJ())
		+ 4
		* Math_Soboutils.ithbit(block.z, this.getJ() - block.getJ());

	}

	public ArrayList<Octree> adjacentGroundCells(DyadicBlock block){
		// return the list of the cell contained in the octree
		// that are adjacent to the DyadicBlock bloc
		ArrayList<Octree> octreeArr = new ArrayList<Octree>();
		this.adjacentGroundCellsInner(block, octreeArr);
		return octreeArr;
	}

	private void adjacentGroundCellsInner(DyadicBlock block, ArrayList<Octree> octreeArr){
		// check if current node is adjacent
		if (this.isAdjacentTo(block) || this.contains(block)){
			if (this.getState() instanceof OctreeStateGround){
				octreeArr.add(this);
			}
			if (this.getState() instanceof OctreeStateFatherCool ||
					this.getState() instanceof OctreeStateFatherWorried)  {
				for (int i = 0;i<8 ;i++){
					this.getSons()[i].adjacentGroundCellsInner(block, octreeArr);
				}
			}

		}
	}


}
