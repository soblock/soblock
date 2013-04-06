package org.wavecraft.gameobject;

import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.Coord3d;

public class GameObject {
	protected Coord3d position;
	protected BoundingBox boundingBox;
	
	public GameObject(){
		this.position=new Coord3d(0, 0, 0);
		this.boundingBox = new BoundingBox(new Coord3d(-1, -1, -1), new Coord3d(1,1,1));
	}
	
	public Coord3d getPosition(){
		return position;
	}
	public BoundingBox getBoundingBox(){
		return boundingBox;
	}
	public BoundingBox getTranslatedBoundingBox(){
		return boundingBox.translate(position);
	}
	public BoundingBox getTranslatedAndDilatedBoundingBox(){
		return boundingBox.translate(position);
	}
}
