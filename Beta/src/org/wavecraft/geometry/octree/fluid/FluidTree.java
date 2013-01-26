package org.wavecraft.geometry.octree.fluid;

import java.util.ArrayList;


import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;

import org.wavecraft.Soboutils.Math_Soboutils;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Coord3i;


import org.wavecraft.geometry.octree.builder.OctreeBuilder;

@SuppressWarnings("serial")
public class FluidTree extends DyadicBlock{

	public static int JMAX = Octree.JMAX;
	protected FluidTree[] sons = null;
	protected FluidTree father = null;
	protected int content = 0;
	public double value;
	boolean isfull=false;




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
	public FluidTree(int x,int y,int z ,int J, int contenu){
		super(x,y,z,J);
		this.father = null;
		this.value=0.;
		this.content=contenu;
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
	public void killMeFather(){
		//father.sons[this.father.findSonContaining(this)]=null;
	}
	public void moveFluid(Octree Terran,Coord3d observerpos,OctreeBuilder builder ){

		moveFluidBis(Terran,observerpos,builder);

		cleanFluidTree(Terran);
	}
	public void removeUnecessaryKids(){
		if (sons!=null){
			int dead_children=0;
			int full_children=0;
			for (int offset = 0; offset < 8; offset++) {
				if (sons[offset]==null || sons[offset].fluidContained()==0 )
					dead_children+=1;
			}
			if (dead_children==8) {
				sons=null;
				return;
			}
			boolean bottom_children_full=true;
			for (int offset = 0; offset < 4; offset++) 
				if (sons[offset]!=null
				&& sons[offset].isfull ) 
					full_children+=1;
				else
					bottom_children_full=false;
			for (int offset = 4; offset < 8; offset++) 
				if (sons[offset]!=null && sons[offset].isfull ) 
					full_children+=1;

			if (full_children==8 || (bottom_children_full && full_children==4)) {value=this.fluidContained(); sons=null; isfull=true;}
		}
	}
	public void cleanFluidTree(Octree Terran){

		if (sons==null) {
			if (value==0) 
				if (father!=null) father.sons[father.findSonContaining(this)]=null;
		}
		else{
			for (int offset=0; offset<8; offset++){
				if (sons[offset]!=null) 
					sons[offset].cleanFluidTree(Terran);
			}
			this.removeUnecessaryKids();
			//if (Terran.smallestCellContaining(this).getJ()>this.getJ()) {value=this.fluidContained(); sons=null; isfull=true;}			
		}
	}
	public void moveFluidBis(Octree Terran,Coord3d observerpos,OctreeBuilder builder ){

		if (value!=0 && sons!=null) System.out.format("echec %n");
		if (value<0) System.out.format("echec negative value %n");
		Octree cube=new Octree(this.x,this.y,this.z,this.getJ());
		if (!builder.cull(cube) && sons!=null){

			isfull=false;
			for (int offset = 0; offset < 8; offset++) {
				if (sons[offset]!=null ) {
					sons[offset].moveFluidBis(Terran, observerpos,builder);
				}
			}

		}
		else{
			double saved=fluidContained();
			if (builder.cull(cube)){
				this.sons=null;
				this.value=saved;
			}
			if (saved<1E-5) {value=0; return;}
			diffuseFluid(Terran); 

			if (this.sons==null ) isfull=true;
		}
	}
	public void diffuseFluid( Octree Terran){
		boolean[] is_wall=nghb_infos_wall();

		FluidTree root=this.findTheRoot();
		if (value==0)  return;
		Coord3i[] c=new Coord3i[6];
		c[0]=new Coord3i( 1, 0, 0);c[1]=new Coord3i(-1, 0, 0);
		c[2]=new Coord3i( 0, 1, 0);c[3]=new Coord3i( 0,-1, 0);
		c[4]=new Coord3i( 0, 0, 1);c[5]=new Coord3i( 0, 0,-1);

		for (int offset=5; offset>-1; offset--){
			if (!is_wall[offset]){	
				FluidTree voisin=new FluidTree(this.x+c[offset].x,this.y+c[offset].y,this.z+c[offset].z,this.getJ());
				voisin=root.getThisBlock(voisin);
				voisin.diffuseIn(this, Terran.smallestCellContaining(voisin));
			}	
		}
		if (this.value<1E-5) {this.value=0.;}
	}

	public FluidTree findTheRoot(){
		if (father!=null) return father.findTheRoot();
		else return this;
	}
	public double fill_with_fluid(double vol, Coord3i c, Octree Terran, FluidTree root){
		FluidTree voisin=new FluidTree(this.x+c.x,this.y+c.y,this.z+c.z,this.getJ());
		voisin=root.getThisBlock(voisin);
		double v=vol;
		if (vol>0 )v=voisin.fill_with_fluid(vol,this, Terran);
		else  v=voisin.removeFluid(vol,this, Terran);
		return v;
	}
	public FluidTree getThisBlock(FluidTree block){
		if (this.getJ()==block.getJ()) return this;
		else{
			if (value!=0) return this;
			else{ 
				if (sons==null) sons=new FluidTree[8];
				int offset=this.findSonContaining(block);
				if (offset<0) return this;
				if (sons[offset]==null){
					sons[offset]=new FluidTree(this.subBlock(offset),this,this.content);
					sons[offset].value=0.;
					sons[offset].father=this;
				}
				return sons[offset].getThisBlock(block);
			}
		}
	}



	public double fill_with_fluid(double vol, FluidTree tree_to_empty, Octree Terran){
		if (sons==null){
			double v=Math.min(vol,Math_Soboutils.dpowerOf2[3*this.getJ()]-value);
			if (v>0){
				boolean[] exist=Terran.doesThisBlockExist(this);
				if ( !exist[0]){	
					value+=v;
					return vol-v;
				}
				else {
					if (exist[1]){
						if (father!=null)father.sons[father.findSonContaining(this)]=null;
						else value=0;
						return vol;//+saved_v;
					}
					else{
						this.sons=new FluidTree[8];
						double saved_v=value;
						value=0;
						return fill_with_fluid(vol+saved_v,tree_to_empty,Terran);
					}
				}
			}
			else return vol;
		}
		else{
			for (int offset=0; offset<8 && vol>0; offset++){
				if (sons[offset]!=null){
					if (sons[offset].are_neighbors(tree_to_empty)) 
						vol=sons[offset].fill_with_fluid(vol,tree_to_empty,Terran.smallestCellContaining(sons[offset]));
				}
				else{
					FluidTree son=this.initSonAndReturnIt(offset);
					boolean[] exist=Terran.doesThisBlockExist(son);
					if ( (!exist[0] || exist[0] && !exist[1]) && son.are_neighbors(tree_to_empty)) {
						sons[offset]=son;
						vol=sons[offset].fill_with_fluid(vol,tree_to_empty,Terran.smallestCellContaining(sons[offset]));
					}						
				}				
			}
			return vol;
		}
	}

	public boolean are_neighbors(FluidTree tree){
		if(  (x  )*Math_Soboutils.powerOf2[getJ()]==(tree.x+1)*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (x+1)*Math_Soboutils.powerOf2[getJ()]==(tree.x  )*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (y  )*Math_Soboutils.powerOf2[getJ()]==(tree.y+1)*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (y+1)*Math_Soboutils.powerOf2[getJ()]==(tree.y  )*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (z  )*Math_Soboutils.powerOf2[getJ()]==(tree.z+1)*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (z+1)*Math_Soboutils.powerOf2[getJ()]==(tree.z  )*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		return false;
	}




	public boolean[] nghb_infos_wall(){
		boolean[] ngbh=new boolean[6];
		
		/// the infos are numbered in a +x,-x,+y,-y,+z,-z
		Octree neigh=new Octree(this.x,this.y,this.z,this.getJ());
		int size_max=Math_Soboutils.powerOf2[JMAX-this.getJ()];
		neigh.x+=1;
		ngbh[1]=(neigh.x<size_max)?false:true;
		neigh.x-=2;
		ngbh[0]=(neigh.x>=0)      ?false:true;
		neigh.x+=1;

		neigh.y+=1;
		ngbh[3]=(neigh.y<size_max)?false:true;
		neigh.y-=2;
		ngbh[2]=(neigh.y>=0)      ?false:true;
		neigh.y+=1;

		neigh.z+=1;
		ngbh[5]=(neigh.z<size_max)?false:true;
		neigh.z-=2;
		ngbh[4]=(neigh.z>=0)      ?false:true;
		neigh.z+=1;

		return ngbh;

	}

	public double fill_this_cube(double vol, Octree Terran){
		if (sons==null){
			double v=Math.max(0,Math.min(vol,Math_Soboutils.dpowerOf2[3*this.getJ()]));
			boolean[] exist=Terran.doesThisBlockExist(this);
			if ( !exist[0]){	
				value=v;
				return vol-v;
			}
			else {
				if (exist[1]){
					if (father!=null){
						father.sons[father.findSonContaining(this)]=null;
					}
					else value=0;
					return vol;
				}
				else 
					if (this.getJ()>0){
						sons=new FluidTree[8];
						value=0;
						return fill_this_cube(vol,Terran);
					}
					else
						return vol;
			}
		}
		else{
			for (int offset=0; offset<8 && vol>0; offset++){
				if (sons[offset]!=null){
					vol=sons[offset].fill_this_cube(vol,Terran);
				}
				else{
					FluidTree son=this.initSonAndReturnIt(offset);
					boolean[] exist=Terran.doesThisBlockExist(son);
					if ( (!exist[0] || exist[0] && !exist[1]) ) {
						sons[offset]=son;
						vol=sons[offset].fill_this_cube(vol,Terran);
					}		
				}
			}
			return vol;
		}
	}




	public void diffuseIn(FluidTree tree, Octree Terran){
		if (sons==null){
			boolean[] exist=Terran.doesThisBlockExist(this);
			if ( !exist[0]){
				double h=  tree.z*Math_Soboutils.dpowerOf2[tree.getJ()]
						-z*Math_Soboutils.dpowerOf2[getJ()];
				h+= tree.value/Math_Soboutils.dpowerOf2[2*tree.getJ()]
						-value/Math_Soboutils.dpowerOf2[2*getJ()];
				double v=h/(1/Math_Soboutils.dpowerOf2[2*getJ()]+1/Math_Soboutils.dpowerOf2[2*tree.getJ()]);
				//v*=1+v/Math.min(Math_Soboutils.fpowerOf2[3*tree.getJ()],Math_Soboutils.fpowerOf2[3*this.getJ()]);
				//v*=1+2*v*v/Math.pow((Math_Soboutils.fpowerOf2[3*tree.getJ()]+Math_Soboutils.fpowerOf2[3*this.getJ()]),2);				
				// cannot add more to this than empty space in this 
				v=Math.min(Math_Soboutils.dpowerOf2[3*getJ()]-value,v);
				// cannot remove more the the volume of tree from tree
				v=Math.min(tree.value, v);
				// cannot add more in tree than empty space in tree
				v=Math.max(v,(-Math_Soboutils.dpowerOf2[3*tree.getJ()]+tree.value));
				// cannot remove more the the volume of this from this
				v=Math.max(v,-value);
				tree.value-=v;
				value+=v;
			}
			else {
				 if (!exist[1] && this.getJ()>0){
					this.sons=new FluidTree[8];
					double vol=value;
					value=0.;
					double trop_plein=this.fill_this_cube(vol, Terran);
					this.take_care_of_trop_plein(trop_plein, Terran.findTheRoot());
					diffuseIn(tree,Terran);
				}
			}
		}
		else{
			for (int offset=0; offset<8; offset++){
				if (sons[offset]!=null){
					if (sons[offset].are_neighbors(tree)) 
						sons[offset].diffuseIn(tree,Terran.smallestCellContaining(sons[offset]));
				}
				else{
					FluidTree son=this.initSonAndReturnIt(offset);
					boolean[] exist=Terran.doesThisBlockExist(son);
					if ( (!exist[0] || exist[0] && !exist[1]) && son.are_neighbors(tree)) {
						sons[offset]=son;
						sons[offset].diffuseIn(tree,Terran.smallestCellContaining(sons[offset]));
					}						
				}
			}
		}
	}
	public void initSon(int offset){
		if (this.sons==null) this.sons= new FluidTree[8];
		FluidTree son=new FluidTree(this.subBlock(offset),this,this.content);
		son.father=this;
		son.value=0;
		son.sons=null;
		this.sons[offset]=son;	
	}
	public FluidTree initSonAndReturnIt(int offset){
		//if (sons==null) this.sons= new FluidTree[8];
		FluidTree son=new FluidTree(this.subBlock(offset),this,this.content);
		son.father=this;
		son.value=0;
		son.sons=null;
		return son;	
	}
	public double removeFluid(double vol, FluidTree tree_to_empty, Octree Terran){
		if (sons==null){
			double v=Math.max(vol,-value);
			if (v<0){
				boolean[] exist=Terran.doesThisBlockExist(this);
				if ( !exist[0]){	
					value+=v;
					return vol-v;
				}
				else {
					if (exist[1]){
						double saved_v=value;
						if (father!=null){
							father.sons[father.findSonContaining(this)]=null;
						}
						else value=0;
						return vol+saved_v;
					}
					else{
						this.sons=new FluidTree[8];
						double saved_v=value;
						value=0;
						return removeFluid(vol+saved_v,tree_to_empty,Terran);
					}
				}
			}
			else return vol;
		}
		else{
			for (int offset=0; offset<8 && vol<0; offset++){
				if (sons[offset]!=null){
					if (sons[offset].are_neighbors(tree_to_empty)) 
						vol=sons[offset].removeFluid(vol,tree_to_empty,Terran.smallestCellContaining(sons[offset]));
				}
				else{
					FluidTree son=this.initSonAndReturnIt(offset);
					boolean[] exist=Terran.doesThisBlockExist(son);
					if ( (!exist[0] || exist[0] && !exist[1]) && son.are_neighbors(tree_to_empty)) {
						sons[offset]=son;
						vol=sons[offset].removeFluid(vol,tree_to_empty,Terran.smallestCellContaining(sons[offset]));
					}						
				}				
			}
			return vol;
		}
	}


	public double take_care_of_trop_plein(double trop_plein,Octree Terran){
		int h=this.z+1;
		int size_max=Math_Soboutils.powerOf2[JMAX-this.getJ()];
		int index=1;
		FluidTree root=this.findTheRoot();
		
		while(trop_plein>0 && h<size_max){
			Coord3i c=new Coord3i( 0,0,index);
			FluidTree voisin=new FluidTree(this.x+c.x,this.y+c.y,this.z+c.z,this.getJ());
			voisin=root.getThisBlock(voisin);
			double saved=voisin.fluidContained();
			trop_plein=voisin.fill_this_cube(trop_plein+saved,Terran);
			h++;
			index++;
		}
		if (trop_plein>0) System.out.format("trop plein %f %n",trop_plein);
		return trop_plein;

	}


	public boolean hasSons(){
		return (sons!=null);
	}
	public int findSonContaining(FluidTree block) {
		return Math_Soboutils.ithbit(block.x, this.getJ() - block.getJ()) + 2
				* Math_Soboutils.ithbit(block.y, this.getJ() - block.getJ())
				+ 4
				* Math_Soboutils.ithbit(block.z, this.getJ() - block.getJ());

	}
	public double fluidContained(){
		if (sons==null) {return value;}
		else{
			double sum=0;

			for(int offset = 0; offset < 8; offset++) {
				if (sons[offset]!=null) sum+=sons[offset].fluidContained();
			}
			return sum;
		}
	}

	public void initializeVolumes(){
		if (sons==null) value=0*Math_Soboutils.dpowerOf2[3*getJ()]/5;
		else{
			value=0.0;
			for (int offset = 0; offset < 8; offset++) {
				if (sons[offset]!=null){
					sons[offset].initializeVolumes();
				}
			}

		}
	}

	public FluidTree smallestBlockContaining(FluidTree block){
		if (this.getJ()==block.getJ()) return this;
		else{
			if (value!=0) return this;
			else{ 
				if (sons==null) return this;
				int offset=this.findSonContaining(block);
				if (offset<0) return this;
				if (sons[offset]==null){
					return this;
				}
				return sons[offset].getThisBlock(block);
			}
		}
	}

	private void rasterizeInner(FluidTree node, ArrayList<DyadicBlock> raster){
		if (node.sons==null){
			if( node.value>0){
				raster.add(node);
			}
		}

		else{
			for (int i =0; i<8; i++){
				if (node.sons[i]!=null){
					//System.out.println(this.toString());
					rasterizeInner(node.sons[i], raster);
				}
			}
		}
	}


}
