package org.wavecraft.graphics.view;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.wavecraft.geometry.Coord2d;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Plane;

public class CameraFPS implements Camera {
	private Coord3d position;
	private Coord2d angles;
	
	public CameraFPS(Coord3d position, Coord2d angles){
		this.position = position;
		this.angles = angles;
	}
	
	public CameraFPS(){}
	
	public void setModelViewMatrix() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		float ux = (float) (Math.cos(angles.x) * Math.cos(angles.y));
		float uy = (float) (Math.sin(angles.x) * Math.cos(angles.y));
		float uz = (float) (Math.sin(angles.y));
		float centerx = (float) position.x + ux;
		float centery = (float) position.y + uy;
		float centerz = (float) position.z + uz;
		float upx = (float) 0;
		float upy = (float) 0;
		float upz = (float) 2;
	
		GLU.gluLookAt((float) position.x,(float) position.y, (float) position.z, centerx, centery, centerz, upx, upy,
				upz);
	}
	

	public void setConfig(Coord3d position,Coord2d angles){
		this.position = position;
		this.angles = angles;
	}
	

	@Override
	public String toString(){
		return String.format("%s %s",position.toString(),angles.toString());
	}
	
	
	/**
	 * 
	 * @param fovy : the field of view in 0y, in degree
	 * @param aspect : the aspect ratio x/y
	 * @return
	 */
	public List<Plane> fourPlane(double fovy, double aspect){
		double ux = Math.cos(angles.x) * Math.cos(angles.y); // front direction
		double uy = Math.sin(angles.x) * Math.cos(angles.y);
		double uz = Math.sin(angles.y);
		Coord3d u = new Coord3d(ux, uy, uz);
		
		double vx = - Math.cos(angles.x) * Math.sin(angles.y); // up direction
		double vy = - Math.sin(angles.x) * Math.sin(angles.y);
		double vz = Math.cos(angles.y);
		Coord3d v = new Coord3d(vx, vy, vz);
		
		double wx = -Math.sin(angles.x) * Math.cos(angles.y); // left direction
		double wy = Math.cos(angles.x) * Math.cos(angles.y);
		double wz = Math.sin(angles.y);
		Coord3d w = new Coord3d(wx, wy, wz);
		
		double fovx = fovy * aspect; // field of view in x
		double fovxRad = fovy*Math.PI/180;
		double fovyRad = fovx*Math.PI/180;
		
		double dtheta = Math.acos(fovyRad/2); // the angle in radian to rotate left 
		double dphi = Math.acos(fovxRad/2); // the angle in radian to rotate up
		
		double cosdth = Math.cos(dtheta);
		double sindth = Math.sin(dtheta);
		double cosdph = Math.cos(dphi);
		double sindph = Math.sin(dphi);
		
		Coord3d normalLeft = w.scalarMult(sindth).addInNewVector(u.scalarMult(-cosdth));
		Plane planeLeft = new Plane(position, normalLeft);
		Coord3d normalRight = w.scalarMult(-sindth).addInNewVector(u.scalarMult(-cosdth));
		Plane planeRight = new Plane(position, normalRight);
		Coord3d normalUp = v.scalarMult(sindph).addInNewVector(u.scalarMult(-cosdph));
		Plane planeUp = new Plane(position, normalUp);
		Coord3d normalDown = v.scalarMult(-sindph).addInNewVector(u.scalarMult(-cosdph));
		Plane planeDown = new Plane(position, normalDown);
		
		List<Plane> listOfPlane = new ArrayList<Plane>(4);
		listOfPlane.add(planeLeft);
		listOfPlane.add(planeRight);
		listOfPlane.add(planeUp);
		listOfPlane.add(planeDown);
		return listOfPlane;
	}

}
