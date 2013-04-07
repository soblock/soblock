package org.wavecraft.gameobjet.save;


import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Terran;

public class GameSaveAtom {

	private DyadicBlock block;
	private Terran terran;
	private double value;
	private long time;
	
	public GameSaveAtom(DyadicBlock block, Terran terran, double value, long time){
		this.block = block;
		this.terran = terran;
		this.value = value;
		this.time = time;
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
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
}
