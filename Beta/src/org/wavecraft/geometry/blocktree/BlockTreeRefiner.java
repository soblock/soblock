package org.wavecraft.geometry.blocktree;

import org.wavecraft.graphics.vbo.VBOBlockTreeGrandFather;
import org.wavecraft.graphics.vbo.VBOBlocktreePool;





public class BlockTreeRefiner implements Runnable {

	public enum State {
		NO_JOB,
		PROCESSING_JOB,
		READY_TO_PROCESS_JOB,
		FINISHED,
	}
	
	private Blocktree nodeToRefine;
	private BlocktreeBuilder builder;
	private State state;
	private VBOBlockTreeGrandFather vbo;
	
	public BlockTreeRefiner(){
		state = State.NO_JOB;
		nodeToRefine = null;
		builder = null;
	}
	
	@Override
	public void run() {
		while (true){
			//System.out.println(getState());
			if (getState() == State.READY_TO_PROCESS_JOB) {
				setState(State.PROCESSING_JOB);
				BlocktreeUpdaterSimple updater = new BlocktreeUpdaterSimple(getBuilder());
				Blocktree.State initialStateOfNode = getNodeToRefine().getState();
				updater.updateInner(getNodeToRefine());
				switch (initialStateOfNode) {
				case GRAND_FATHER:
					vbo = new VBOBlockTreeGrandFather(nodeToRefine);
					break;

				default:
					vbo = null;
					break;
				}
				setState(State.FINISHED);
			}
			else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
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
		if (vbo !=null){
			VBOBlocktreePool.getInstance().put(getNodeToRefine(), getVbo());
		} else {
			VBOBlocktreePool.getInstance().prepareToUnload(getNodeToRefine());
		}
	}

	

	
	
}
