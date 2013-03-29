package org.wavecraft.geometry.worldfunction;

public enum Climate {
	DESERT, 
	ARTIC,
	JUNGLE,
	FOREST;
	
	public static Climate getClimate(double humidity, double temperature){
		if ((humidity < 0.5 && temperature < 0.5) || temperature < 0.2){
			return ARTIC;
		}
		if ((humidity < 0.5 && temperature > 0.5) || temperature > 0.8){
			return DESERT;
		}
		if (humidity > 0.5 && temperature < 0.5){
			return FOREST;
		}
		return JUNGLE;
	}
}
