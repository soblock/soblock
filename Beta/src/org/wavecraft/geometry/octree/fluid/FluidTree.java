package org.wavecraft.geometry.octree.fluid;

import java.util.ArrayList;

import org.lwjgl.Sys;
import org.wavecraft.geometry.DyadicBlock;

@SuppressWarnings("serial")
public class FluidTree extends DyadicBlock{

	
	protected FluidTree[] sons = null;
	protected FluidTree father = null;
	protected int content = 0;
	public double value=0;
	boolean isfull=false;
	boolean hasBeenTreated=false;
	


	public FluidTree(DyadicBlock block, FluidTree father,int contenu){
		super(block.x,block.y,block.z,block.getJ());
		this.father = null;
		this.content=contenu;
		this.value=0.;
	}


	public FluidTree(int x,int y,int z ,int J){
		super(x,y,z,J);
		this.father = null;
		this.value=0.;

	}
	
	public ArrayList<DyadicBlock> rasterize(){
		ArrayList<DyadicBlock> raster = new ArrayList<DyadicBlock>();
		rasterizeInner(this, raster);
		return raster;
	}
	
	
	public void initSons(){
		sons = new FluidTree[8];
	}
	
	public FluidTree[] getSons(){
		return sons;
	}
	
	private void rasterizeInner(FluidTree node, ArrayList<DyadicBlock> raster){
		if (node.value>0){
			raster.add(node);
		}
		if (node.sons!=null){
			for (int i =0; i<8; i++){
				if (node.sons[i]!=null){
					//System.out.println(this.toString());
					rasterizeInner(node.sons[i], raster);
				}
			}
		}
	}
	

}
