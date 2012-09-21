package org.wavecraft.client;




// client timer (singleton)
public class Timer {
	private static Timer timer = null;
	
	private static double lastTime;
	private static double currTime;
	private static int nFrame;
	
	private Timer(){
		lastTime = System.currentTimeMillis();
		currTime = System.currentTimeMillis();
		nFrame = 0;
	}
	
	public static Timer getTimer(){
		if (timer == null){
			timer = new Timer();
		}
		return timer;
	}
	
	public static double getDt(){
		return currTime - lastTime;
	}
	
	public static double getCurrT(){
		return currTime;
	}
	
	public static int getNframe(){
		return nFrame;
	}
	
	public static void update(){
		lastTime = currTime;
		currTime = System.currentTimeMillis();
		nFrame++;
	}
}
