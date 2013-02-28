package org.wavecraft.geometry.blocktree;

import org.wavecraft.graphics.vbo.VBOBlockTreeGrandFather;
import org.wavecraft.graphics.vbo.VBOBlocktreePool;

import java.util.ArrayList;
import java.util.List;




public class BlockTreeRefiner implements Runnable {

	public enum State {
		NO_JOB,
		PROCESSING_JOB,
		READY_TO_PROCESS_JOB,
		FINISHED,
	}
	
	public enum StateVBO{
		UPLOAD_ME_UNLOAD_SONS,
		UNLOAD_ME_UPLOAD_SONS,
		DO_NOTHING
	}
	
	private StateVBO vboState;
	private Blocktree nodeToRefine;
	private BlocktreeBuilder builder;
	private State state;
	private VBOBlockTreeGrandFather vbo;
	private List<VBOBlockTreeGrandFather> vboSons;
	
	public BlockTreeRefiner(){
		state = State.NO_JOB;
		nodeToRefine = null;
		builder = null;
		vboSons = new ArrayList<VBOBlockTreeGrandFather>();
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
					if (getNodeToRefine().getState() == Blocktree.State.PATRIARCH){ // the node has been split : prepare to unload it
						// and add its sons
						
						vboSons.clear();
						for (int i=0; i<8; i++){
							Blocktree son = nodeToRefine.getSons()[i];
							if (son.getState()==Blocktree.State.GRAND_FATHER){
								VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(son);
								vboSons.add(nextVbo);
							} else {
								vboSons.add(null);
							}
						}
						vboState = StateVBO.UNLOAD_ME_UPLOAD_SONS;
					}
					break;

				case PATRIARCH:
					if (getNodeToRefine().getState() == Blocktree.State.GRAND_FATHER){ // the node has been merged : unload it
						VBOBlockTreeGrandFather nextVbo = new VBOBlockTreeGrandFather(nodeToRefine);
						vbo = nextVbo;
						vboState = StateVBO.UNLOAD_ME_UPLOAD_SONS;
					}
					break;
				default:
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
		case UPLOAD_ME_UNLOAD_SONS:
			VBOBlocktreePool.getInstance().put(getNodeToRefine(), getVbo());
			for (Blocktree grandsons : nodeToRefine.listOfSonsOfStateGrandFather()){
				VBOBlocktreePool.getInstance().prepareToUnload(grandsons);
			}
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
	
		default:
			break;
		}
		vboState = StateVBO.DO_NOTHING;
	}

	

	
	
}
