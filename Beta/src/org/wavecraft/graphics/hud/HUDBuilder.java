package org.wavecraft.graphics.hud;

import org.wavecraft.stats.StatisticsClient;

public class HUDBuilder {
	public static HUD defaultHUD(){
		HUD hud = new HUDDebug();
		return hud;
	}
	
	public static void bind(HUD hud){
		if (hud instanceof HUDDebug){
			((HUDDebug) hud).setFpsGraphPanel(new GraphPanel(StatisticsClient.getFpsGraph(),0,60,1));
			((HUDDebug) hud).setEventCountPanel(new GraphPanel(StatisticsClient.getEventCountGraph(), 0, 10,2));
		}
		
	}
}
