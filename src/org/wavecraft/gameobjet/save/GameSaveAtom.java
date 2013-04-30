package org.wavecraft.gameobjet.save;


import java.io.Serializable;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Terran;

public class GameSaveAtom implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5490533085766037989L;

	public enum Type {
		ADD,
		REMOVE
	}
	
	private DyadicBlock block;
	private Terran terran;
	private long time;
	private Type type;
	
	
	public GameSaveAtom(DyadicBlock block, Terran terran, long time, Type type){
		this.block = block;
		this.terran = terran;
		this.time = time;
		this.type = type;
	}

	/**
	 * @return the block
	 */
	public DyadicBlock getBlock() {
		return block;
	}

	/**
	 * @return the terran
	 */
	public Terran getTerran() {
		return terran;
	}


	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
}
