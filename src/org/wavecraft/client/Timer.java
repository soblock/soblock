package org.wavecraft.client;




// client timer (singleton)
public class Timer {
	private static Timer timer = null;
	
	private static double lastTime;
	private static double currTime;
	private static int nFrame;
	
	private Timer(){
		lastTime = System.nanoTime();
		currTime = System.nanoTime();
		nFrame = 0;
	}
	
	public static Timer getTimer(){
		if (timer == null){
			timer = new Timer();
		}
		return timer;
	}
	
	public static double getDt(){
		return (currTime - lastTime)/1E6;
	}
	
	public static double getCurrT(){
		return currTime/1E6;
	}
	
	public static int getNframe(){
		return nFrame;
	}
	
	public static void update(){
		lastTime = currTime;
		currTime = System.nanoTime();
		nFrame++;
	}
}
