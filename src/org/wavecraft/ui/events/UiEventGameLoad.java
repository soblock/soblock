package org.wavecraft.ui.events;

import org.wavecraft.gameobjet.save.GameSave;

public class UiEventGameLoad implements UiEvent {
	public UiEventGameLoad(GameSave gameSave) {
		super();
		this.gameSave = gameSave;
	}

	private GameSave gameSave;

	/**
	 * @return the gameSave
	 */
	public GameSave getGameSave() {
		return gameSave;
	}

	/**
	 * @param gameSave the gameSave to set
	 */
	public void setGameSave(GameSave gameSave) {
		this.gameSave = gameSave;
	}
	

}
