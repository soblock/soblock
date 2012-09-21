package org.wavecraft.graphics.vbo;

import java.util.ArrayList;
import java.util.HashMap;


import org.wavecraft.client.Timer;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventListener;
import org.wavecraft.graphics.vbo.VBOWrapper.VboMode;

public class VBOOctree implements OctreeEventListener {
	private HashMap<Octree, Integer> map;
	// double buffering node to destroy
	public ArrayList<Octree> toDestroyNext;
	public ArrayList<Octree> toDestroy;
	private boolean[] isDead;
	private int position;
	private int size;
	private int stride;
	private int verticePerBlock;
	private VBOWrapper vbo;

	public VBOOctree(int size){
		this.size = size;
		stride = 10 * 4; // number of float per vertices * 4 bytes per float
		verticePerBlock = 4 * 6; // 4 vertices * 6 faces
		float[] initArr = new float[size * stride * verticePerBlock];
		toDestroyNext = new ArrayList<Octree>();
		toDestroy = new ArrayList<Octree>();
		vbo = new VBOWrapper(VboMode.V3N3C4);
		vbo.initFromFloat(initArr);
		isDead = new boolean[size];
		for (int i = 0; i< size ; i++){
			isDead[i] = true;
		}
		position = 0;
		map = new HashMap<Octree, Integer>();
	}

	private void findNextDead(){
		int savePose = position;
		position++;
		if (position>=size) position = 0;
		while (!isDead[position] && position !=savePose){
			position++;
			if (position>=size) position = 0;
		}
		if (position == savePose){
			System.out.println("buffer is full");
			System.out.println(Timer.getNframe());
		}
	}

	public void pushNode(Octree node){
		findNextDead();
		float[] buff = BlockToArray.toArray(node);
		vbo.update(buff, verticePerBlock*position);
		map.put(node, new Integer(position));
		isDead[position] = false;
	}

	public void removeNode(Octree node){
		if (map.containsKey(node)){
			int positionToRemove = map.get(node);
			map.remove(node);
			Octree octreetmp = new Octree(new DyadicBlock(positionToRemove%255,-16, positionToRemove/255 , 0), null);
			float[] buff = BlockToArray.toArray(octreetmp);
			vbo.update(buff, verticePerBlock*positionToRemove);
			isDead[positionToRemove] = true;
		}
		else{
			//System.out.println(" node do not belongs to vbo");
			//System.out.println(node.toString2());
		}
	}
	
	public void draw(){
		vbo.draw();
	}

	public void update(){
		for (int i=0;i<toDestroy.size();i++){
			removeNode(toDestroy.get(i));
		}
		toDestroy = toDestroyNext;
		toDestroyNext = new ArrayList<Octree>();
	}
	@Override
	public void handle(OctreeEvent e) {
		// TO RE DO
	}


}
