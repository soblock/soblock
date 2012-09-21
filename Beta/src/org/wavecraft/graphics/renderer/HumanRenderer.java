package org.wavecraft.graphics.renderer;

import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.Coord3d;

public class HumanRenderer {
	
	private static  Coord3d scales = new Coord3d(3, 5, 12);
	
	
	public static BoundingBox legLeftBB(){
		Coord3d minCoords = new Coord3d(0, 1, 1);
		Coord3d maxCoords = new Coord3d(1, 2, 4);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox legRightBB(){
		Coord3d minCoords = new Coord3d(0, 3, 1);
		Coord3d maxCoords = new Coord3d(1, 4, 4);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox footLeftBB(){
		Coord3d minCoords = new Coord3d(0, 1, 0);
		Coord3d maxCoords = new Coord3d(2, 2, 1);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox footRightBB(){
		Coord3d minCoords = new Coord3d(0, 3, 0);
		Coord3d maxCoords = new Coord3d(2, 4, 1);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox bodyBB(){
		Coord3d minCoords = new Coord3d(0, 1, 4);
		Coord3d maxCoords = new Coord3d(2, 4, 8);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox armLeftBB(){
		Coord3d minCoords = new Coord3d(0, 0, 4);
		Coord3d maxCoords = new Coord3d(1, 1, 8);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox armRightBB(){
		Coord3d minCoords = new Coord3d(0, 4, 4);
		Coord3d maxCoords = new Coord3d(1, 5, 8);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox handLeftBB(){
		Coord3d minCoords = new Coord3d(0, 0, 3);
		Coord3d maxCoords = new Coord3d(1, 1, 4);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox handRightBB(){
		Coord3d minCoords = new Coord3d(0, 4, 3);
		Coord3d maxCoords = new Coord3d(1, 5, 4);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox neckBB(){
		Coord3d minCoords = new Coord3d(0, 2, 8);
		Coord3d maxCoords = new Coord3d(1, 3, 9);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox headBB(){
		Coord3d minCoords = new Coord3d(0, 1, 9);
		Coord3d maxCoords = new Coord3d(2.5, 4, 12);
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	public static BoundingBox sunglassBB(){
		Coord3d minCoords = new Coord3d(2.6, 0.9, 10.5);
		Coord3d maxCoords = new Coord3d(3, 4.1,11 );
		minCoords.scale3d(scales);
		maxCoords.scale3d(scales);
		return new BoundingBox(minCoords, maxCoords);
	}
	
	public static void colorSkin(){
		GL11.glColor3d(255/255.0, 153/255.0, 153/255.0);
	}
	
	public static void colorLeg(){
		GL11.glColor3d(51/255.0, 153/255.0, 255/255.0);
	}
	
	public static void colorBody(){
		GL11.glColor3d(51/255.0, 204/255.0, 102/255.0);
	}
	
	public static void colorSunglass(){
		GL11.glColor3d(30/255.0, 30/255.0, 30/255.0);
	}
	
	
}
