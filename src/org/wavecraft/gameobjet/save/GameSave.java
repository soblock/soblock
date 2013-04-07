package org.wavecraft.gameobjet.save;

import java.util.ArrayList;
import java.util.List;


import org.wavecraft.geometry.worldfunction.WorldFunction;

/**
 * contains all instances required to properly save a game
 * @author laurentsifre
 *
 */
public class GameSave {
	
	private WorldFunction worldFunction;
	
	private List<GameSaveAtom> listOfAtoms;
	
	
	public GameSave(){
		this.listOfAtoms = new ArrayList<GameSaveAtom>();
	}
	
	public void addAtom(GameSaveAtom atom){
		listOfAtoms.add(atom);
	}

	/**
	 * @param worldFunction the worldFunction to set
	 */
	public void setWorldFunction(WorldFunction worldFunction) {
		this.worldFunction = worldFunction;
	}
	
	/**
	 * @return the worldFunction
	 */
	public WorldFunction getWorldFunction() {
		return worldFunction;
	}
	
	
	/**
	 * writes this gamesave into the specified file
	 * @param path
	 */
	public void write(String path){
		// TODO : max 
	}
	
	public GameSave load(String path){
		// TODO : max
		return null;
	}


}
