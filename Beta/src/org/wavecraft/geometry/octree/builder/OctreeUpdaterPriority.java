package org.wavecraft.geometry.octree.builder;

import java.util.ArrayList;

import org.wavecraft.client.Timer;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeState;
import org.wavecraft.geometry.octree.OctreeStateDead;
import org.wavecraft.geometry.octree.OctreeStateFatherCool;
import org.wavecraft.geometry.octree.OctreeStateFatherWorried;
import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.OctreeStateNotYetVisited;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.stats.Profiler;


public class OctreeUpdaterPriority implements OctreeUpdater {
	// this class implements the update of the octree using several priority bins.
	// ALGO : 
	// 1 - on fait un premier parcours dans larbre et on met chaque noeud dans la bin 
	// correspondant a sa priorite. (petite valeur de priorite traite en first)
	// 2 - on traite un certain nombre de node avec un budget en commencant par les bins de plus 
	// forte priorite

	private int nBins;
	private int nFamily;
	private Octree[][] bins;
	private int[] binPos;
	private int[] binCapacity;
	private double maxPriority ;

	private Octree octree;
	private OctreeBuilder builder;
	private float budgetPerFramePerFamily;


	private double percentageOfUpdate = 1/1.0;
	private double nFrameWithoutUpdate = 1;

	@SuppressWarnings("unchecked")
	public OctreeUpdaterPriority(Octree octree, OctreeBuilder builder){
		this.octree = octree;
		this.builder = builder;
		nBins = 16;
		nFamily= 1;
		this.maxPriority = 64;
		budgetPerFramePerFamily = 10000000; // huge budget at the begining
		
		bins = new Octree[nBins*nFamily][];
		binPos = new int[nBins*nFamily];
		binCapacity = new int[nBins*nFamily];
		for (int i = 0 ; i<nBins*nFamily ; i++){
			int initCapacity = 1000000;
			bins[i] = new Octree[initCapacity];
			
			binPos[i] = 0;
		}
	}

	private void emptyBins(){
		for (int i = 0 ; i<nBins ; i++){
			//bins[i].clear();
			for (int j = 0;j<binPos[i];j++){
				bins[i][j]= null;
			}
			binPos[i]=0;
			//bins[i] = new ArrayList<Octree>(100000);
		}
	}

	private void putNodeInBins(){
		putNodeInBinsInner(octree);
	}

	private int getFamily(Octree octree){
		OctreeState state = octree.getState();
		// may be split or destroyed
		if (state instanceof OctreeStateLeaf ||
				state instanceof OctreeStateNotYetVisited ){
			return 0;
		}
		// may be merged
		if (state instanceof OctreeStateFatherCool  ){
			return 0;
		}
		return 0;
	}

	private void putNodeInBinsInner(Octree node){
		if (Math.random() < percentageOfUpdate){

			int nodeFamily = getFamily(node);
			if (nodeFamily>=0){
				double priorityValue = builder.priority(node);
				int binIndice = (int) ((nBins-1)* Math.min(priorityValue/maxPriority,1));
				//System.out.println(binIndice);
				int trueBinInd = binIndice + nBins*nodeFamily;
				
				bins[trueBinInd][binPos[trueBinInd]] = node;
				binPos[trueBinInd]++;
				
				//bins[binIndice + nBins*nodeFamily].add(node);
				//System.out.println(binIndice);
				//System.out.println(node.toString2());
			}
		}
		if (node.hasSons()){
			for (int i = 0 ; i<8 ; i++){
				putNodeInBinsInner(node.getSons()[i]);
			}
		}
	}

	private void processBins(){

		for (int iFamily = 0; iFamily<nFamily ; iFamily++){
			float budget = budgetPerFramePerFamily;
			int iBin = 0;// indice of the bin
			int iNode = 0;// indice of the node inside the bin
			while (budget > 0 && iBin < nBins){
				//if (iNode<bins[iBin  + nBins*iFamily ].size()){
				if (iNode<binPos[iBin  + nBins*iFamily ]){
					//Octree node = bins[iBin  + nBins*iFamily].get(iNode);
					Octree node = bins[iBin  + nBins*iFamily][iNode];
					double res =node.getState().internalJob(node, builder);
					budget -= res;
					iNode++;
					if (res>0){
						//System.out.format("%d %d %f %s %s %n ",iNode,iBin,budget,node.toString2(),node.getState().getClass().toString());
					}
				}
				else{
					iNode = 0;
					iBin++;
				}
			}
		}
	}
	@Override
	public void updateOctree() {

		if (Timer.getNframe()==30){
			budgetPerFramePerFamily = 32;
			percentageOfUpdate = 1/8.0;
		}
		
		if (Timer.getNframe()%nFrameWithoutUpdate == 0){
			double t1 = System.currentTimeMillis();
			emptyBins();
			double t2 = System.currentTimeMillis();
			putNodeInBins();
			double t3 = System.currentTimeMillis();
			processBins();
			double t4 = System.currentTimeMillis();
			OctreeEventMediator.notifyAllListener();
			double t5 = System.currentTimeMillis();
			if (Timer.getNframe()%10 == 0) {
				//System.out.format("empty %f put %f process %f  event %f %n",t2 - t1, t3 - t2, t4 -t3, t5 - t4);
			}
			Profiler.getInstance().push("OctreeEmptyBins", t2-t1, Timer.getCurrT());
			Profiler.getInstance().push("OctreeputNodeInBins", t3-t2, Timer.getCurrT());
			Profiler.getInstance().push("OctreeProcessBins", t4-t3, Timer.getCurrT());
			Profiler.getInstance().push("OctreeEventMediator", t5-t4, Timer.getCurrT());
		}
	}

}
