package org.wavecraft.geometry.octree.builder;


import javax.swing.text.html.HTMLDocument.HTMLReader.BlockAction;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.cullingpriority.Culler;
import org.wavecraft.geometry.octree.cullingpriority.CullerPosition;
import org.wavecraft.geometry.octree.cullingpriority.OctreePriorityFunction;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionUtils;
import org.wavecraft.geometry.worldfunction.WorldFunction;
import org.wavecraft.modif.ModifOctree;

public class OctreeBuilderWorldFuntionCullerModif implements OctreeBuilder{
	private WorldFunction worldFunction;
	private Culler culler;
	private OctreePriorityFunction priorityFun;
	private ModifOctree modif;


	public  OctreeBuilderWorldFuntionCullerModif(WorldFunction worldFunction,ModifOctree modif, Culler culler){
		this.worldFunction = worldFunction;
		this.modif=modif;
		this.culler = culler;
		if (culler instanceof CullerPosition){
			this.priorityFun = (OctreePriorityFunction) culler;
		}
	}

	@Override
	public boolean cull(Octree octree) {
		return culler.cull(octree);
	}
	@Override
	public boolean isOutsideDomainOfInterest(Octree octree) {
		//return isOutsideDomainOfInterestSinglePrecision(octree);
		return isOutsideDomainOfInterestArbitrayPrecision(octree, 3);
	}



	public ModifOctree getModifOctree(){
		return modif;
	}
	
	public WorldFunction getWorldFunction(){
		return worldFunction;
	}
	
	public boolean isOutsideDomainOfInterestSinglePrecision(Octree octree) {
		ModifOctree cell=modif.smallestCellContainingBlock(octree);
		double sumAncestors, bmax,bmin,v;
		if (cell.getJ()>octree.getJ() && cell.sons!=null){
			sumAncestors=cell.sumAncestors;
			v=cell.value; 
			bmin=0.;
			bmax=0.;
			//bmin=cell.boundMin;
			//bmax=cell.boundMax;
		}
		else{
			sumAncestors=cell.sumAncestors;
			bmin=cell.boundMin;
			bmax=cell.boundMax;
			v=cell.value;
		}
		double[] minmax =  ThreeDimFunctionUtils.minMaxValuesAtVertices(worldFunction, octree);
		double vMin=minmax[0]+sumAncestors+v+bmin;
		double vMax=minmax[1]+sumAncestors+v+bmax;
		double JumpMax=modif.jumpMax(octree);
		double Dphi = worldFunction.uncertaintyBound(octree);
		return ( vMin>Dphi // air
				|| vMax+JumpMax<-Dphi); // ground
	}



	// for arbitrary precision checking there are two situations.
	// 1) we are not culled, we just proceed a single precision test.
	// our children will be tested anyway at arbitrary precision 
	// and they will tell us to worry if they happen to be dead
	// 2) we are culled, so are sons are not created and we dont know if
	// they belongs to the surface or not.
	// we will recursively and TEMPORARY create them to evaluate the function on them
	// and count how many of sons are dead.
	// if N = 4 for example, it is equivalent to check the min max value on
	// a grid 16x16 =256 instead of just checking our 8 vertices.
	// This is a bit costly but increase A LOT the attainable precision
	// Since isOutsideOfInterest is only called once per node, it is acceptable.
	public boolean isOutsideDomainOfInterestArbitrayPrecision(Octree octree, int N){
		// if im not culled, proceed as usual
		if (!cull(octree) || N ==0 || octree.getJ() == 0){
			//if (N ==0 || octree.getJ() == 0){
			return isOutsideDomainOfInterestSinglePrecision(octree);
		}
		else {
			//System.out.println(octree.toString2());
			octree.initSonsQuietly();
			int deadSons = 0;
			for (int offset = 0; offset< 8 ; offset++){
				// check if we need to go deeper or not.
				if (isOutsideDomainOfInterestSinglePrecision(octree.getSons()[offset])){
					deadSons++;
				}
				else {
					if (isOutsideDomainOfInterestArbitrayPrecision(octree.getSons()[offset],N-1)){
						deadSons++;
					}
				}
			}
			octree.killSonsQuietly();
			return (deadSons == 8);
		}
	}

	@Override
	public double priority(Octree octree) {
		return priorityFun.priority(octree);
	}

	@Override
	public void setContent(Octree octree) {
		// TODO Auto-generated method stub
		ModifOctree smallestMod = modif.smallestNegativeCellContainingBlock(octree);
		if (smallestMod != null && smallestMod.value<0){
			octree.setContent(smallestMod.content);
			//System.out.println(smallestMod.toString());
		}
		else {
			octree.setContent(worldFunction.contentAt(octree));
		}
		
	}

	@Override
	public boolean isUpperFace(Face face) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGround(Octree octree) {
//		// BUG FIX LAURENT 13 10 2012
//		// l'horrible bug des trous venait d'ici:
//		// il ne faut pas verifier les conditions dincertitude ici
//		// car ici on est suppose les avoir deja teste !!
//		// en particulier on risque de les avoir teste avec la fonction recursive
//		// avec une precision arbitraire et donc si on refait le test en 
//		// non recursif la precision risque de ne pas etre suffisante
//		// alors que l'on a deja fait les test...
//		// donc on fait BEAUCOUP plus simple on regarde juste si le centre est negatif
//		
		ModifOctree cell=modif.smallestCellContainingBlock(octree);
		double S, bmax,v;
		if (cell.getJ()>octree.getJ() && cell.sons!=null){
			S=cell.sumAncestors;v=cell.value;bmax=0.;
		}
		else{
			S=cell.sumAncestors;bmax=cell.boundMax;v=cell.value;
		}
		double[] minmax =  ThreeDimFunctionUtils.minMaxValuesAtVertices(worldFunction, octree);
		double vMax=minmax[1]+S+v+bmax;
//		double JumpMax=modif.jumpMax(octree);
//		if (Math.abs(JumpMax ) >0) System.out.println(JumpMax);
//		double Dphi = worldFunction.uncertaintyBound(octree);
//		if (octree.x == 0 && octree.y == 11 && octree.z == 32 && octree.getJ() ==2)
//		{
//			System.out.println("WTF");
//		}
		//return ( vMax+JumpMax<-Dphi);
	
		return (vMax<0);
		//return (worldFunction.valueAt(octree.center()) < 0);
		
	}
}

