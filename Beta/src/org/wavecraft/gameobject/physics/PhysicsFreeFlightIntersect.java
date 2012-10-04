package org.wavecraft.gameobject.physics;

import java.util.ArrayList;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.gameobject.GameObjectMoving;
import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.octree.Octree;

public class PhysicsFreeFlightIntersect extends PhysicsFreeFlight{

	public PhysicsFreeFlightIntersect(){
		super();
	}

	@Override
	public void move(GameObjectMoving movingObject, double dt) {
		super.move(movingObject, dt);
		// check for collision
		Octree root = GameEngine.getOctree();
		BoundingBox box = movingObject.getTranslatedBoundingBox();

		ArrayList<Octree> listOfIntersectedLeaf = CollisionDetectionOctree.intersectedLeaf(root, box);
		boolean case1happened = false;
		boolean case2happened = false;
		boolean case3happened = false;
		boolean case4happened = false;
		boolean case5happened = false;
		boolean case6happened = false;
		for (int i=0; i<listOfIntersectedLeaf.size(); i++){
			Octree leaf = listOfIntersectedLeaf.get(i);
			BoundingBox box2 = new BoundingBox(leaf);
			// 6 case to check :
			
			// box over box2
			if (!case1happened){
				double dzm1M2 = box.getMinCoord3d().z - box2.getMaxCoord3d().z;  
				double dzm1m2 = box.getMinCoord3d().z - box2.getMinCoord3d().z;
				if (dzm1M2 < 0 && dzm1m2 >0){
					movingObject.getPosition().add(new Coord3d(0, 0, -0.001-dzm1M2));
					case1happened = true;
					case2happened = true;
				}
			}
			// box under box2
			if (!case2happened ){
				double dzM1m2 = box.getMaxCoord3d().z - box2.getMinCoord3d().z;  
				double dzM1M2 = box.getMaxCoord3d().z - box2.getMaxCoord3d().z;
				if (dzM1m2 > 0 && dzM1M2 <0){
					movingObject.getPosition().add(new Coord3d(0, 0, -dzM1m2));
					case2happened = true;
					case1happened = true;
				}
			}
			// box over box2
			if (!case3happened){
				double dxm1M2 = box.getMinCoord3d().x - box2.getMaxCoord3d().x;  
				double dxm1m2 = box.getMinCoord3d().x - box2.getMinCoord3d().x;
				if (dxm1M2 < 0 && dxm1m2 >0){
					movingObject.getPosition().add(new Coord3d(-dxm1M2, 0,0 ));
					case3happened = true;
					case4happened = true;
				}
			}
			// box under box2
			if (!case4happened){
				double dxM1m2 = box.getMaxCoord3d().x - box2.getMinCoord3d().x;  
				double dxM1M2 = box.getMaxCoord3d().x - box2.getMaxCoord3d().x;
				if (dxM1m2 > 0 && dxM1M2 <0){
					movingObject.getPosition().add(new Coord3d(-dxM1m2, 0,0 ));
					case4happened = true;
				}
			}
			// box over box2
			if (!case5happened){
				double dym1M2 = box.getMinCoord3d().y - box2.getMaxCoord3d().y;  
				double dym1m2 = box.getMinCoord3d().y - box2.getMinCoord3d().y;
				if (dym1M2 < 0 && dym1m2 >0){
					movingObject.getPosition().add(new Coord3d(0,-dym1M2,0 ));
					case5happened = true;
				}
			}
			// box under box2
			if (!case6happened){
				double dyM1m2 = box.getMaxCoord3d().y - box2.getMinCoord3d().y;  
				double dyM1M2 = box.getMaxCoord3d().y - box2.getMaxCoord3d().y;
				if (dyM1m2 > 0 && dyM1M2 <0){
					movingObject.getPosition().add(new Coord3d(0,-dyM1m2,0 ));
					case6happened = true;
				}
			}



		}
	}

}
