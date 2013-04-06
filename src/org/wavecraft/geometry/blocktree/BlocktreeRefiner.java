package org.wavecraft.geometry.blocktree;

import org.wavecraft.graphics.vbo.VBOBlockTreeGrandFather;
import org.wavecraft.graphics.vbo.VBOBlocktreePool;

import java.util.ArrayList;
import java.util.List;




public class BlocktreeRefiner implements Runnable {

	public enum State {
		NO_JOB,
		PROCESSING_JOB,
		READY_TO_PROCESS_JOB,
		FINISHED,
	}

	public enum StateVBO{
		UPLOAD_ME_UNLOAD_SONS,
		UNLOAD_ME_UPLOAD_SONS,
		REUPLOAD_ME,
		DO_NOTHING
	}

	private boolean regenate;


	private StateVBO vboState;
	private Blocktree nodeToRefine;
	private BlocktreeBuilder builder;
	private State state;
	private VBOBlockTreeGrandFather vbo;
	private List<VBOBlockTreeGrandFather> vboSons;
	private List<Blocktree> sonsBefore;
	private boolean active;
	private boolean verboseDebug = false;

	public BlocktreeRefiner(){
		state = State.NO_JOB;
		nodeToRefine = null;
		builder = null;
		vboSons = new ArrayList<VBOBlockTreeGrandFather>();
		setActive(true);
	}

	@Override
	public void run(){
		while (isActive()){
			if (getState() == State.READY_TO_PROCESS_JOB) {
				Blocktree.State initialStateOfNode = getNodeToRefine().getState();
				if (isRegenate()){
					switch (initialStateOfNode) {
					case GRAND_FATHER:
						runRegenerateGrandFather();
						break;

						
					case PATRIARCH:
						runRegeneratePatriarch();
						break;
					default:
							throw new IllegalArgumentException("can regenerate only grand father");
					}

				} else {
					switch (initialStateOfNode) {
					case GRAND_FATHER:
						runGrandFather();
						break;

					case PATRIARCH:
						runPatiarch();
						break;

					default:
						break;
					}
				}
			}
		}
	}


	//
	//	@Override
	//	public void run() {
	//		while (isActive()){
	//			//System.out.println(getState());
	//			if (getState() == State.READY_TO_PROCESS_JOB) {
	//				double t1 = System.currentTimeMillis();
	//				setState(State.PROCESSING_JOB);
	//				BlocktreeUpdaterSimple updater = new BlocktreeUpdaterSimple(getBuilder());
	//				Blocktree.State initialStateOfNode = getNodeToRefine().getState();
	//				//updater.updateInner(getNodeToRefine());
	//				switch (initialStateOfNode) {
	//				case GRAND_FATHER:
	//					updater.splitAllLeaf(getNodeToRefine());
	//					break;
	//
	//				case PATRIARCH:
	//					sonsBefore = getNodeToRefine().listOfSonsOfStateGrandFather();
	//					updater.mergeAllLeaf(getNodeToRefine());
	//				default:
	//					break;
	//				}
	//				double t2 = System.currentTimeMillis();
	//				switch (initialStateOfNode) {
	//				case GRAND_FATHER:
	//					if (getNodeToRefine().getState() == Blocktree.State.PATRIARCH){ // the node has been split : prepare to unload it
	//						// and add its sons
	//
	//						vboSons.clear();
	//						for (int i=0; i<8; i++){
	//							Blocktree son = nodeToRefine.getSons()[i];
	//							if (son.getState()==Blocktree.State.GRAND_FATHER){
	//								//VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(son);
	//								VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(son, builder);
	//								vboSons.add(nextVbo);
	//							} else {
	//								vboSons.add(null);
	//							}
	//						}
	//						vboState = StateVBO.UNLOAD_ME_UPLOAD_SONS;
	//					}
	//					break;
	//
	//				case PATRIARCH:
	//					if (getNodeToRefine().getState() == Blocktree.State.GRAND_FATHER){ // the node has been merged : upload it
	//						//VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(nodeToRefine);
	//						VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(nodeToRefine, builder);
	//						vbo = nextVbo;
	//						vboState = StateVBO.UPLOAD_ME_UNLOAD_SONS;
	//					}
	//					break;
	//				default:
	//					break;
	//				}
	//				double t3 = System.currentTimeMillis();
	//				if (verboseDebug){
	//					System.out.println("BlocktreeRefiner : refine "+ (t2-t1)+ " faces "+(t3-t2));
	//				}
	//				setState(State.FINISHED);
	//			}
	//			else {
	//				try {
	//					Thread.sleep(100);
	//				} catch (InterruptedException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		}
	//
	//	}

	public void runGrandFather(){
		double t1 = System.currentTimeMillis();
		setState(State.PROCESSING_JOB);
		BlocktreeUpdaterSimple updater = new BlocktreeUpdaterSimple(getBuilder());
		updater.splitAllLeaf(getNodeToRefine());
		double t2 = System.currentTimeMillis();
		if (nodeToRefine.getState() == Blocktree.State.PATRIARCH){ // the node has been split : prepare to unload it
			// and add its sons
			vboSons.clear();
			for (int i=0; i<8; i++){
				Blocktree son = nodeToRefine.getSons()[i];
				if (son.getState()==Blocktree.State.GRAND_FATHER){
					VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(son, builder);
					vboSons.add(nextVbo);
				} else {
					vboSons.add(null);
				}
			}
			vboState = StateVBO.UNLOAD_ME_UPLOAD_SONS;
		}
		double t3 = System.currentTimeMillis();
		if (verboseDebug){
			System.out.println("BlocktreeRefiner : refine "+ (t2-t1)+ " faces "+(t3-t2));
		}
		setState(State.FINISHED);
	}

	public void runPatiarch(){
		double t1 = System.currentTimeMillis();
		setState(State.PROCESSING_JOB);
		BlocktreeUpdaterSimple updater = new BlocktreeUpdaterSimple(getBuilder());
		sonsBefore = getNodeToRefine().listOfSonsOfStateGrandFather();
		updater.mergeAllLeaf(getNodeToRefine());
		double t2 = System.currentTimeMillis();
		if (nodeToRefine.getState() == Blocktree.State.GRAND_FATHER){ // the node has been merged : upload it
			VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(nodeToRefine, builder);
			vbo = nextVbo;
			vboState = StateVBO.UPLOAD_ME_UNLOAD_SONS;
		}
		double t3 = System.currentTimeMillis();
		if (verboseDebug){
			System.out.println("BlocktreeRefiner : refine "+ (t2-t1)+ " faces "+(t3-t2));
		}
		setState(State.FINISHED);
	}

	public void runRegenerateGrandFather(){
		double t1 = System.currentTimeMillis();
		BlocktreeUpdaterSimple updater = new BlocktreeUpdaterSimple(getBuilder());
		updater.init(getNodeToRefine());
		double t2 = System.currentTimeMillis();
		VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(getNodeToRefine(), builder);
		vbo = nextVbo;
		vboState = StateVBO.REUPLOAD_ME;
		double t3 = System.currentTimeMillis();
		if (verboseDebug){
			System.out.println("BlocktreeRefiner : refine "+ (t2-t1)+ " faces "+(t3-t2));
		}
		setState(State.FINISHED);
	}
	
	public void runRegeneratePatriarch(){
		double t1 = System.currentTimeMillis();
		sonsBefore = getNodeToRefine().listOfSonsOfStateGrandFather();
		BlocktreeUpdaterSimple updater = new BlocktreeUpdaterSimple(getBuilder());
		updater.init(getNodeToRefine());
		double t2 = System.currentTimeMillis();
		VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(getNodeToRefine(), builder);
		vbo = nextVbo;
		double t3 = System.currentTimeMillis();
		if (verboseDebug){
			System.out.println("BlocktreeRefiner : refine "+ (t2-t1)+ " faces "+(t3-t2));
		}
		vboState = StateVBO.UPLOAD_ME_UNLOAD_SONS;
		setState(State.FINISHED);
		
	}

	public synchronized void  setVbo(VBOBlockTreeGrandFather vbo) {
		this.vbo = vbo;
	}

	public synchronized VBOBlockTreeGrandFather getVbo() {
		return vbo;
	}

	public synchronized Blocktree getNodeToRefine() {
		return nodeToRefine;
	}

	public synchronized void setNodeToRefine(Blocktree nodeToRefine) {
		this.nodeToRefine = nodeToRefine.cloneRecursively();
	}

	public synchronized State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public synchronized void setBuilder(BlocktreeBuilder builder) {
		this.builder = builder;
	}

	public synchronized BlocktreeBuilder getBuilder() {
		return builder;
	}

	public void doInMainThreadWhenDone(){
		switch (vboState) {
		case UPLOAD_ME_UNLOAD_SONS: // in this order !
			for (Blocktree grandsons : sonsBefore){
				VBOBlocktreePool.getInstance().prepareToUnload(grandsons);
			}
			VBOBlocktreePool.getInstance().put(getNodeToRefine(), getVbo());
			break;

		case UNLOAD_ME_UPLOAD_SONS:
			VBOBlocktreePool.getInstance().prepareToUnload(getNodeToRefine());
			for (int i=0; i<8; i++){
				VBOBlockTreeGrandFather vbo = vboSons.get(i);
				if (vbo!=null){
					Blocktree son = nodeToRefine.getSons()[i];
					VBOBlocktreePool.getInstance().put(son, vbo);
				}
			}
			break;

		case REUPLOAD_ME:
			VBOBlocktreePool.getInstance().prepareToUnload(getNodeToRefine());
			VBOBlocktreePool.getInstance().put(getNodeToRefine(), vbo);
			break;

		default:
			break;
		}
		vboState = StateVBO.DO_NOTHING;
	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}



	public synchronized boolean isRegenate() {
		return regenate;
	}

	public synchronized void setRegenate(boolean regenate) {
		this.regenate = regenate;
	}



}
