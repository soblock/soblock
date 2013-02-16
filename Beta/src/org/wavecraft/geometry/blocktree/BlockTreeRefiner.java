package org.wavecraft.geometry.blocktree;





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
	
	public BlockTreeRefiner(){
		state = State.NO_JOB;
		nodeToRefine = null;
		builder = null;
	}
	
	@Override
	public void run() {
		while (true){
			System.out.println(getState());
			if (getState() == State.READY_TO_PROCESS_JOB) {
				setState(State.PROCESSING_JOB);
				BlocktreeUpdaterSimple updater = new BlocktreeUpdaterSimple(getBuilder());
				updater.updateInner(getNodeToRefine());
				setState(State.FINISHED);
			}
			else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	public synchronized Blocktree getNodeToRefine() {
		return nodeToRefine;
	}

	public synchronized void setNodeToRefine(Blocktree nodeToRefine) {
		this.nodeToRefine = nodeToRefine.cloneRecursively();
	}

	public State getState() {
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


	

	
	
}
