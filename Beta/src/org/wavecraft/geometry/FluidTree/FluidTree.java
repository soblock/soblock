package org.wavecraft.geometry.FluidTree;

import org.wavecraft.Soboutils.Math_Soboutils;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Coord3i;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;

import org.wavecraft.geometry.octree.builder.OctreeBuilder;

public class FluidTree extends DyadicBlock {
	public static int JMAX = Octree.JMAX;
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


	public void move_fluid_bis_treat_once_every_cell(Octree Terran,Coord3d observerpos,OctreeBuilder builder ){

		move_fluid_bis(Terran,observerpos,builder);

		cleanFluidTree(Terran);
	}
	public void cleanFluidTree(Octree Terran){

		if (sons==null) {
			if (value==0) 
				if (father!=null) father.sons[father.findSonContaining(this)]=null;
		}
		else{
			int dead_children=0;
			int full_children=0;
			for (int offset=0; offset<8; offset++){
				if (sons[offset]==null) dead_children++;
				else {
					sons[offset].cleanFluidTree(Terran);
					if (sons[offset]==null) dead_children++;
				}
			}
			if (dead_children==8 && father!=null) father.sons[father.findSonContaining(this)]=null;
			for (int offset = 0; offset < 8; offset++) {if (sons[offset]!=null && sons[offset].isfull ) full_children+=1;}
			if (full_children==8) {value=this.fluidContained(); sons=null; isfull=true;}
		}
	}
	public void move_fluid_bis(Octree Terran,Coord3d observerpos,OctreeBuilder builder ){

		if (value!=0 && sons!=null) System.out.format("echec %n");
		if (value<0) System.out.format("echec negative value %n");
		Octree cube=new Octree(this.getX(),this.getY(),this.getZ(),this.getJ());
		if (!builder.cull(cube)){

			int dead_children=0;
			int full_children=0;
			isfull=false;
			for (int offset = 0; offset < 8; offset++) {
				if (sons[offset]!=null ) {
					sons[offset].move_fluid_bis(Terran, observerpos,builder);
				}
			}
			for (int offset = 0; offset < 8; offset++) {if (sons[offset]==null || sons[offset].fluidContained()==0 ) dead_children+=1;}
			for (int offset = 0; offset < 8; offset++) {if (sons[offset]!=null && sons[offset].isfull ) full_children+=1;}
			if (dead_children==8) sons=null;
			//	if (Terran.find_smallest_block_containing_block(this).J>this.J) {value=this.fluid_contained(); sons=null; isfull=true;}
			if (full_children==8) {value=this.fluidContained(); sons=null; isfull=true;}
		}
		else{
			double saved=fluidContained();
			sons=null;
			if (saved<1E-5) {value=0; sons=null;return;}
			if (saved<1E-3) {content=5;}
			else{content=4;}
			double trop_plein=fill_this_cube(saved, Terran);
			if (trop_plein>0) take_care_of_trop_plein(trop_plein, Terran);
			if (sons==null){
				diffuseFluid(Terran); isfull=true;
			}
			else{
				isfull=false;
				for (int offset = 0; offset < 8; offset++) {
					if (sons[offset]!=null ) {
						sons[offset].move_fluid_bis(Terran, observerpos,builder);
					}
				}
			}
		}
	}
	public void diffuseFluid( Octree Terran){
		boolean[] is_wall=nghb_infos(Terran);

		if (!is_wall[5]){
			Coord3i c=new Coord3i( 0,0,-1);
			value=fill_with_fluid(value,c,Terran);	
			if (value==0) {sons=null; value=0.; return;}
		}

		if (!is_wall[4]){
			Coord3i c=new Coord3i( 0,0,1);
			double v=-Math_Soboutils.dpowerOf2[3*this.getJ()]+value;
			value+=-v+fill_with_fluid(v,c,Terran);		
		}

		if (value==0) return;
		FluidTree root=findTheRoot();
		Coord3i[] c=new Coord3i[4];
		c[0]=new Coord3i( 1, 0,0);c[0]=new Coord3i(-1,0,0);
		c[2]=new Coord3i( 0, 1,0);c[3]=new Coord3i( 0,-1,0);

		for (int offset=0; offset<4; offset++){
			if (!is_wall[offset]){	
				FluidTree voisin=new FluidTree(this.getX()+c[offset].getX(),this.getY()+c[offset].getY(),this.getZ()+c[offset].getZ(),this.getJ());
				voisin=root.getThisBlock(voisin);
				voisin.diffuseIn(this, Terran);
			}	
		}
	}
	public FluidTree findTheRoot(){
		if (father!=null) return father.findTheRoot();
		else return this;
	}
	public double fill_with_fluid(double vol, Coord3i c, Octree Terran){
		FluidTree root=findTheRoot();
		FluidTree voisin=new FluidTree(this.getX()+c.getX(),this.getY()+c.getY(),this.getZ()+c.getZ(),this.getJ());
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
					this.hasBeenTreated=true;
					return vol-v;

				}
				else {
					if (exist[1]){
						double saved_v=value;
						if (father!=null)father.sons[father.findSonContaining(this)]=null;
						else value=0;
						return vol+saved_v;

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
			int dead_children=0;
			for (int offset=0; offset<8 && vol>0; offset++){
				if (sons[offset]!=null){
					if (sons[offset].are_neighbors(tree_to_empty)) vol=sons[offset].fill_with_fluid(vol,tree_to_empty,Terran);
				}
				else{
					FluidTree son=this.initSon(offset);
					boolean[] exist=Terran.doesThisBlockExist(son);
					if ( (!exist[0] || exist[0] && !exist[1]) && son.are_neighbors(tree_to_empty)) {
						sons[offset]=son;
						vol=sons[offset].fill_with_fluid(vol,tree_to_empty,Terran);
					}						
				}				
				if (sons[offset]==null || !(sons[offset].fluidContained()>0)) dead_children++;
			}
			if (dead_children==8) sons=null;
			return vol;
		}
	}

	public boolean are_neighbors(FluidTree tree){
		if(  (getX()  )*Math_Soboutils.powerOf2[getJ()]==(tree.getX()+1)*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (getX()+1)*Math_Soboutils.powerOf2[getJ()]==(tree.getX()  )*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (getY()  )*Math_Soboutils.powerOf2[getJ()]==(tree.getY()+1)*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (getY()+1)*Math_Soboutils.powerOf2[getJ()]==(tree.getY()  )*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (getZ()  )*Math_Soboutils.powerOf2[getJ()]==(tree.getZ()+1)*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		if(  (getZ()+1)*Math_Soboutils.powerOf2[getJ()]==(tree.getZ()  )*Math_Soboutils.powerOf2[tree.getJ()]) return true;
		return false;
	}




	public boolean[] nghb_infos(Octree Terran){
		boolean[] ngbh=new boolean[6];
		Octree neigh=new Octree(this.getX(),this.getY(),this.getZ(),this.getJ());
		int size_max=Math_Soboutils.powerOf2[JMAX-this.getJ()];
		neigh.x+=1;
		ngbh[0]=(neigh.x<size_max)?false:true;
		neigh.x-=2;
		ngbh[1]=(neigh.x>=0)      ?false:true;
		neigh.x+=1;

		neigh.y+=1;
		ngbh[2]=(neigh.y<size_max)?false:true;
		neigh.y-=2;
		ngbh[3]=(neigh.y>=0)      ?false:true;
		neigh.y+=1;

		neigh.z+=1;
		ngbh[4]=(neigh.z<size_max)?false:true;
		neigh.z-=2;
		ngbh[5]=(neigh.z>=0)      ?false:true;
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
				else{
					sons=new FluidTree[8];
					value=0;
					//System.out.format("value = %f saved value = %f %n", value, saved_v);
					return fill_this_cube(vol,Terran);
				}
			}
		}
		else{
			int dead_children=0;
			for (int offset=0; offset<8 && vol>0; offset++){
				if (sons[offset]!=null){
					vol=sons[offset].fill_this_cube(vol,Terran);
				}
				else{
					FluidTree son=this.initSon(offset);
					boolean[] exist=Terran.doesThisBlockExist(son);
					if ( (!exist[0] || exist[0] && !exist[1]) ) {
						sons[offset]=son;
						vol=sons[offset].fill_this_cube(vol,Terran);
					}		
				}
				//if (sons[offset]==null || sons[offset].fluid_contained()==0) dead_children+=1;
			}
			//if (dead_children==8) sons=null;
			return vol;
		}
	}




	public double diffuseIn(FluidTree tree, Octree Terran){
		if (sons==null){
			boolean[] exist=Terran.doesThisBlockExist(this);
			if ( !exist[0]){
				double h=tree.getZ()*Math_Soboutils.dpowerOf2[tree.getJ()]-getZ()*Math_Soboutils.dpowerOf2[getJ()];
				h+=       tree.value/Math_Soboutils.dpowerOf2[2*tree.getJ()]-value/Math_Soboutils.dpowerOf2[2*getJ()];
				double v=Math.min(Math_Soboutils.dpowerOf2[3*getJ()]-value,h/(1/Math_Soboutils.dpowerOf2[2*getJ()]+1/Math_Soboutils.dpowerOf2[2*tree.getJ()]));
				v=Math.min(tree.value, v);
				v=Math.max(v,(-Math_Soboutils.dpowerOf2[3*tree.getJ()]+tree.value));
				v=Math.max(v,-value);
				tree.value-=v;
				value+=v;
				return -v;
			}
			else {
				if (exist[1]){
					double saved_v=fluidContained();
					value=0.;
					sons=null;
					tree.value+=saved_v;
					return saved_v;
				}
				else{
					this.sons=new FluidTree[8];
					value=0;
					return diffuseIn(tree,Terran);
				}
			}
		}
		else{
			int dead_children=0;
			double vol=0;
			for (int offset=0; offset<8; offset++){
				if (sons[offset]!=null){
					if (sons[offset].are_neighbors(tree)) vol+=sons[offset].diffuseIn(tree,Terran);
				}
				else{
					FluidTree son=this.initSon(offset);
					boolean[] exist=Terran.doesThisBlockExist(son);
					if ( (!exist[0] || exist[0] && !exist[1]) && son.are_neighbors(tree)) {
						sons[offset]=son;
						vol+=sons[offset].diffuseIn(tree,Terran);
					}						
				}				
				if (sons[offset]==null || !(sons[offset].fluidContained()>0)) dead_children++;
			}
			if (dead_children==8) sons=null;
			return vol;
		}
	}
	public FluidTree initSon(int offset){
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
					this.hasBeenTreated=true;
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
			int dead_children=0;
			for (int offset=0; offset<8 && vol<0; offset++){
				if (sons[offset]!=null){
					if (sons[offset].are_neighbors(tree_to_empty)) vol=sons[offset].removeFluid(vol,tree_to_empty,Terran);
				}
				else{
					FluidTree son=this.initSon(offset);
					boolean[] exist=Terran.doesThisBlockExist(son);
					if ( (!exist[0] || exist[0] && !exist[1]) && son.are_neighbors(tree_to_empty)) {
						sons[offset]=son;
						vol=sons[offset].removeFluid(vol,tree_to_empty,Terran);
					}						
				}				
				if (sons[offset]==null || !(sons[offset].fluidContained()>0)) dead_children++;
			}
			if (dead_children==8) sons=null;
			return vol;
		}
	}


	public double take_care_of_trop_plein(double trop_plein,Octree Terran){
		int h=this.getZ()+1;
		int size_max=Math_Soboutils.powerOf2[JMAX-this.getJ()];
		int index=1;
		FluidTree root=this.findTheRoot();
		while(trop_plein>0 && h<size_max){
			Coord3i c=new Coord3i( 0,0,index);
			FluidTree voisin=new FluidTree(this.getX()+c.getX(),this.getY()+c.getY(),this.getZ()+c.getZ(),this.getJ());
			voisin=root.getThisBlock(voisin);
			double saved=voisin.fluidContained();
			voisin.sons=null;
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
		if (sons==null) value=Math_Soboutils.dpowerOf2[3*getJ()]/10;
		else{
			value=0.0;
			for (int offset = 0; offset < 8; offset++) {
				if (sons[offset]!=null){
					sons[offset].initializeVolumes();
				}
			}

		}
	}
}
