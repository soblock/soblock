package org.wavecraft.geometry.octree.builder;



import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.cullingpriority.Culler;
import org.wavecraft.geometry.octree.cullingpriority.CullerPosition;
import org.wavecraft.geometry.octree.cullingpriority.OctreePriorityFunction;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionUtils;
import org.wavecraft.geometry.worldfunction.WorldFunction;


public class OctreeBuilderWorldFuntionCuller implements OctreeBuilder{
	private WorldFunction worldFunction;
	private Culler culler;
	private OctreePriorityFunction priorityFun;



	public  OctreeBuilderWorldFuntionCuller(WorldFunction worldFunction, Culler culler){
		this.worldFunction = worldFunction;

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


	public boolean isOutsideDomainOfInterestSinglePrecision(Octree octree) {

		double[] minmax =  ThreeDimFunctionUtils.minMaxValuesAtVertices(worldFunction, octree);
		double vMin=minmax[0];
		double vMax=minmax[1];
		double Dphi = worldFunction.uncertaintyBound(octree);
		return ( vMin>Dphi|| vMax<-Dphi);
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
	public double priority(DyadicBlock block) {
		return priorityFun.priority(block);
	}

	@Override
	public void setContent(Octree octree) {
		// TODO Auto-generated method stub
		octree.setContent(worldFunction.contentAt(octree));
	}

	@Override
	public boolean isUpperFace(Face face) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGround(Octree octree) {
		// TODO Auto-generated method stub
		double[] minmax =  ThreeDimFunctionUtils.minMaxValuesAtVertices(worldFunction, octree);
		double vMax=minmax[1];
		double Dphi = worldFunction.uncertaintyBound(octree);
		//return (  vMax<-Dphi);
		return (  vMax<0);
		
	}

	@Override
	public int contentAt(DyadicBlock block) {
		return worldFunction.contentAt(block);
	}
}


