package org.wavecraft.gameobjet.save;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.worldfunction.WorldFunction;
import org.wavecraft.ui.events.UiEventGameLoad;
import org.wavecraft.ui.events.UiEventMediator;


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
	
	public void writeSaveInFile(String filename) throws IOException{
		File file = new File(filename);
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream stream = new ObjectOutputStream(fos);
		stream.writeObject(gameSave);
		stream.close();
		fos.close();
	}
	
	public void loadAnApply(String filename) throws IOException, ClassNotFoundException {
		File file = new File(filename);
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream stream = new ObjectInputStream(fis);
		GameSave save = (GameSave) stream.readObject();
		stream.close();
		fis.close();

		this.gameSave = save;
		UiEventMediator.getUiEventMediator().addEvent(new UiEventGameLoad(gameSave));
		
	}
	
}
