package org.wavecraft.gameobjet.save;


/**
 * a singleton that can be called anywhere to save all modfis
 * @author laurentsifre
 *
 */
public class GameSaveManager {

	private GameSaveManager(){
		this.gameSave = new GameSave(); 
	} // noninstantiable
	
	private static GameSaveManager instance;
	
	private GameSave gameSave;
	
	/**
	 * static factory method
	 * @return the singleton 
	 */
	public static GameSaveManager getInstance(){
		if (instance == null){
			instance = new GameSaveManager();
		}
		return instance;
	}

	/**
	 * @return the gameSave
	 */
	public GameSave getGameSave() {
		return gameSave;
	}
}
