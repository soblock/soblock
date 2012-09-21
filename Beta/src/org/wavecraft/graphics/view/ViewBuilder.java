package org.wavecraft.graphics.view;

import org.wavecraft.gameobject.GameObjectMovingOriented;
import org.wavecraft.geometry.Coord3d;

public class ViewBuilder {
	public static View viewFullFrameFPS(){
		View view = new View();
		view.camera = new CameraFPS();
		view.projection = new ProjectionPerspective();
		return view;
	}
	
	public static View viewFullFrameThirdPerson(){
		View view = new View();
		view.camera = new CameraThirdPerson();
		view.projection = new ProjectionPerspective();
		return view;
	}
	
	public static View viewSmallFrameMinimap(){
		View view = new View();
		view.camera = new CameraTopdown();
		view.projection = new ProjectionOrtho(0.8, 1, 0.8, 1);
		return view;
	}
	
	public static View viewGraph(int i){
		View view = new View();
		view.camera = new CameraTopdown();
		((CameraTopdown) view.camera).setPosition(new Coord3d(0,0,0));
		view.projection = new ProjectionOrtho(0.8 , 1 , 0.2*(i-1), 0.2*i);
		((ProjectionOrtho) view.projection).setRadius(1);
		return view;
	}
	
	public static View viewMenu(){
		View view = new View();
		view.camera = new CameraTopdown();
		((CameraTopdown) view.camera).setPosition(new Coord3d(0,0,0));
		view.projection = new ProjectionOrtho(0,1,0,1);
		((ProjectionOrtho) view.projection).setRadius(1);
		return view;
	}
	
	public static void bind(GameObjectMovingOriented OMObject,View view){
		if (view.camera instanceof CameraFPS){
			((CameraFPS) view.camera).setConfig(OMObject.getPosition(), OMObject.getAngles());
		}
		if (view.camera instanceof CameraThirdPerson){
			((CameraThirdPerson) view.camera).setConfig(OMObject.getPosition(), OMObject.getAngles());
		}
		if (view.camera instanceof CameraTopdown){
			((CameraTopdown) view.camera).setPosition(OMObject.getPosition());
		}
	}
}
