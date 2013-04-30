package org.wavecraft.gameobjet.save;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import org.wavecraft.geometry.worldfunction.WorldFunction;

/**
 * contains all instances required to properly save a game
 * @author laurentsifre
 *
 */
public class GameSave implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1370534697929154607L;

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
	 * @return the listOfAtoms
	 */
	public List<GameSaveAtom> getListOfAtoms() {
		return listOfAtoms;
	}
	
	

}
