package org.wavecraft.graphics.hud;

public class HUDDebug implements HUD {

	
	private GraphPanel fpsGraphPanel;
	//private GraphPanel eventCountPanel;
	
	
	@Override
	public void draw() {
		fpsGraphPanel.draw();
		//eventCountPanel.draw();
	}
	
	public void setFpsGraphPanel(GraphPanel fpsGraphPanel){
		this.fpsGraphPanel = fpsGraphPanel;
	}
	
//	public void setEventCountPanel(GraphPanel eventCountPanel){
//		this.eventCountPanel = eventCountPanel;
//	}
//	
	
}
