package org.wavecraft.graphics.renderer;

// this class builds and provides GameObjectRenderer
public class GameObjectRendererBuilder {
	public static GameObjectRenderer defaultGameObjectRenderer(){
		//return new GameObjectRendererDraw();
		//return new GameObjectRendererBoundingBox();
		return new GameObjectRendererPoint();
	}
}
