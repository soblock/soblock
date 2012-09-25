package org.wavecraft.geometry.octree;

import org.wavecraft.Soboutils.Math_Soboutils;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.modif.ModifOctree;
import org.wavecraft.geometry.FluidTree.FluidTree;



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
	public Octree find_the_root(){
		if (father!=null) return father.find_the_root();
		else return this;
	}
	
	public boolean[] doesThisBlockExist(FluidTree tree){
		Octree tree1=new Octree(tree.getX(),tree.getY(),tree.getZ(),tree.getJ());
		return this.doesThisBlockExist(tree1);
	}
	public boolean[] doesThisBlockExist(Octree tree){
		boolean[] res=new boolean [2];
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
