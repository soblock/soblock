package org.wavecraft.geometry.octree;

import java.util.ArrayList;

import org.wavecraft.Soboutils.Math_Soboutils;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.geometry.octree.fluid.FluidTree;



@SuppressWarnings("serial")
// put every thing application-specific ( fluid, modif etc... ) in state
public class Octree extends DyadicBlock{
	public static int JMAX = 8;
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

	public void initSon(int offset){
		if (sons==null) this.sons= new Octree[8];
		Octree son=new Octree(this.subBlock(offset),this);
		son.father=this;
		son.sons=null;
		this.sons[offset]=son;	
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
	public Octree findTheRoot(){
		if (father!=null) return father.findTheRoot();
		else return this;
	}

	public int findSonContaining(DyadicBlock block) {
		return Math_Soboutils.ithbit(block.x, this.getJ() - block.getJ()) + 2
		* Math_Soboutils.ithbit(block.y, this.getJ() - block.getJ())
		+ 4
		* Math_Soboutils.ithbit(block.z, this.getJ() - block.getJ());

	}

	
	public ArrayList<Octree> adjacentCells(DyadicBlock block, OctreeState state){
		// return the list of the cell contained in the octree
				// that are adjacent to the DyadicBlock bloc
				// and have a state either Ground ore not yet visited
				ArrayList<Octree> octreeArr = new ArrayList<Octree>();
				this.adjacentInner(block, octreeArr, state);
				return octreeArr;
	}

	

	private void adjacentInner(DyadicBlock block, ArrayList<Octree> octreeArr, OctreeState state){
		
		// check if current node is adjacent
		if (this.isAdjacentTo(block) || this.contains(block)){
			if (this.getState().getClass() == state.getClass()){
				if (this.isAdjacentTo(block)){
					octreeArr.add(this);
				}
			}
			if (this.getState() instanceof OctreeStateFatherCool ||
					this.getState() instanceof OctreeStateFatherWorried)  {
				for (int i = 0;i<8 ;i++){
					this.getSons()[i].adjacentInner(block, octreeArr,state);
				}
			}

		}
	}
	

	public boolean[] doesThisBlockExist(FluidTree tree){
		Octree tree1=new Octree(tree.x,tree.y,tree.z,tree.getJ());
		return this.doesThisBlockExist(tree1);
	}
	public boolean[] doesThisBlockExist(Octree tree){
		boolean[] res=new boolean [2];
		if (this.getState() instanceof OctreeStateDead){
			res[0]=false;
			res[1]=false;
			return res;
		}
		else{
			res[1]=(sons==null)?true:false;
			if (this.getJ()==tree.getJ() ){
				{res[0]=true; return res;}
			}
			else{
				if (sons==null) {res[0]=true; return res;}
				int offset= findSonContaining(tree);
				if ( offset<0 || sons[offset]==null){
					{res[0]=false; return res;}
				}
				else{
					return sons[offset].doesThisBlockExist(tree);
				}
			}
		}
	}


}
