package org.wavecraft.gameobject.physics;

//import Coord3d;


import org.wavecraft.Soboutils.Math_Soboutils;

import java.util.ArrayList;
import java.util.EnumSet;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.gameobject.GameObjectMoving;
import org.wavecraft.gameobject.GameObjectMovingOriented;
import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardDown;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

public class PhysicsWalkIntersect extends Physics implements UiEventListener{

	private EnumSet<Direction> direction; // a combination of possible Direction values 
	private double scalarSpeedMult = 1;
	private double scalarSpeedDefault = 0.005;
	Coord3d velocity = new Coord3d(0, 0, 0);
	Coord3d velocitynm1 = new Coord3d(0, 0, 0);
	Coord3d targetVelocity = new Coord3d(0, 0, 0);
	double alphaFricitonGround= 400 / 1000.0f ;
	double alphaFricitonAir= 20 / 1000.0f ;
	double gravityG = 2*9.81 / 1000.0f/ 1000.0f;
	double non_lin_friction_coeff= 0.002;
	double jumpCoefft = Math.sqrt(gravityG*1.1 )*1000;
	double shockCoeff=0.5;
	
	private static PhysicsWalkIntersect instance;
	public static PhysicsWalkIntersect getInstance(){
		if (instance == null){
			instance = new PhysicsWalkIntersect();
		}
		instance.scalarSpeedMult =1; // each time we switch on this, reinit value
		instance.velocity = new Coord3d(0, 0, 0);
		return instance;
	}

	protected PhysicsWalkIntersect(){
		scalarSpeedMult = 1;
		direction = EnumSet.noneOf(Direction.class);
		UiEventMediator.addListener(this);
	}


	public void getTargetVelocity(GameObjectMoving movingObject){
		if (movingObject instanceof GameObjectMovingOriented){
			double theta = ((GameObjectMovingOriented) movingObject).getTheta();
			double phi = ((GameObjectMovingOriented) movingObject).getPhi();
			double ux = (Math.cos(theta) * Math.cos(phi));
			double uy = (Math.sin(theta) * Math.cos(phi));
			double uz = (Math.sin(phi));
			//TODO fucked up bug, don't understand why the commented stuff doesnt work ... If used, in some cases when 
			/// switch to walk mode, scalarSpeedMult seems not nto be initialized and thus has a extrem. large value ....
			double scalarSpeed = scalarSpeedMult * scalarSpeedDefault;
			//System.out.printf("speed %f %f \n",scalarSpeedMult,scalarSpeedDefault);
//			System.out.printf("velocity : %f %f %f %f \n",ux,uy,uz,scalarSpeed);
			targetVelocity = new Coord3d(0, 0, 0);
			// Max 19/09/12: forward/backward: motion only in the x-y plan (should be faster for avoidblock)
			if (direction.contains(Direction.FORWARD)){
				Coord3d speedToAdd = new Coord3d(scalarSpeed*ux, scalarSpeed*uy,0);
				targetVelocity.add(speedToAdd);
			}
			if (direction.contains(Direction.BACKWARD)){
				Coord3d speedToAdd = new Coord3d(-scalarSpeed*ux,- scalarSpeed*uy,0);
				targetVelocity.add(speedToAdd);
			}
			if (direction.contains(Direction.LEFT)){
				Coord3d speedToAdd = new Coord3d(-scalarSpeed*uy, scalarSpeed*ux, 0);
				targetVelocity.add(speedToAdd);
			}
			if (direction.contains(Direction.RIGHT)){
				Coord3d speedToAdd = new Coord3d(scalarSpeed*uy, -scalarSpeed*ux, 0);
				targetVelocity.add(speedToAdd);
			}
			if (direction.contains(Direction.UP) && velocity.z==0.){
				Coord3d speedToAdd = new Coord3d(0,0, jumpCoefft* scalarSpeed);
				targetVelocity.add(speedToAdd);
			}
		}
		//System.out.println(targetVelocity.toString());

	}


	
	public void frictionAccel(double timeDelta){
		if (velocity.z==0){
			velocity.add((targetVelocity.addInNewVector(velocity.scalarMult(-1)).scalarMult(alphaFricitonGround)));
		}
		else {
			//do not affect vertical speed 
			double saveVz=velocity.z;
			velocity.add((targetVelocity.addInNewVector(velocity.scalarMult(-1)).scalarMult(alphaFricitonAir)));
			velocity.z=saveVz;
		}
	}

	public Coord3d gravity() {
		return new Coord3d(0, 0, -gravityG);
	}
	//nonlinear friction
	public Coord3d nonLinearFriction() {
		return new Coord3d(0, 0, -velocity.z*Math.abs(velocity.z)*non_lin_friction_coeff);
	}


	@Override
	public void move(GameObjectMoving movingObject, double dt) {

		if (movingObject instanceof GameObjectMovingOriented){
			getTargetVelocity(movingObject); 
			frictionAccel(dt); // accelerate along the target velocity
			velocity.add(gravity().scalarMult(dt));
			velocity.add(nonLinearFriction().scalarMult(dt));
			movingObject.setSpeed(velocity);
			Octree root = GameEngine.getOctree();
			BoundingBox box = movingObject.getTranslatedBoundingBox();
			box= box.extrude(velocity.scalarMult(dt));
			ArrayList<Octree> listOfIntersectedLeaf = CollisionDetectionOctree.intersectedLeaf(root, box);
			boolean shock_z=false;
			velocitynm1= new Coord3d(velocity);
			avoid_blocks(listOfIntersectedLeaf, movingObject, dt,shock_z);
			direction.clear();
			scalarSpeedMult = 1;
		}
	}

	@Override
	public void handle(UiEvent event) {
		if (event instanceof UiEventKeyboardDown) {

			switch (((UiEventKeyboardDown) event).key) {
			case KEYBOARD_MOVE_FORWARD:
				direction.add(Direction.FORWARD);
				break;
			case KEYBOARD_MOVE_BACKWARD:
				direction.add(Direction.BACKWARD);
				break;
			case KEYBOARD_MOVE_STRAFELEFT:
				direction.add(Direction.LEFT);
				break;
			case KEYBOARD_MOVE_STRAFERIGHT:
				direction.add(Direction.RIGHT);
				break;
			case KEYBOARD_MOVE_UP:
				if(velocity.z==0) direction.add(Direction.UP);
				break;
			case KEYBOARD_SPEED_UP1:
				scalarSpeedMult =4;
				break;
			case KEYBOARD_SPEED_UP2:
				scalarSpeedMult =16;
				break;
			case KEYBOARD_SPEED_UP3:
				scalarSpeedMult =64;
				break;
			case KEYBOARD_SPEED_UP4:
				scalarSpeedMult =256;
				break;
			default:
				break;
			}
		}

	}


	public void avoid_blocks(ArrayList<Octree> listOfIntersectedLeaf,GameObjectMoving movingObject, double dt,boolean shock_z) {
		// / the player is a considered a sphere of radius size_player
		double s = 0.125;
		double h = 1;//1/s;
		double size_cell;
		double dt_save = dt;
		double cx_min, cx_max, cy_min, cy_max, cz_min, cz_max, t;
		boolean is_blocked_x = false;
		boolean is_blocked_y = false;
		boolean is_blocked_z = false;
		double ux = velocity.x;
		double uy = velocity.y;
		double uz = velocity.z;
	

		//double dxnorm=Math.sqrt(velocity.x*velocity.x+velocity.y*velocity.y+velocity.z*velocity.z)*dt;

		Coord3d position=movingObject.getPosition();
		double eyex = position.x;
		double eyey = position.y;
		double eyez = position.z;

		double eps = 0.1;//dxnorm/dt;// eyez+=speed;


		for (int i = 0; i < listOfIntersectedLeaf.size(); i++) {
			Octree leaf = listOfIntersectedLeaf.get(i);
			size_cell = Math_Soboutils.dpowerOf2[leaf.getJ()];


			// bb of current cell
			cx_min = leaf.center().x - size_cell / 2;// Math.pow(2,block[i].coord.x);
			cy_min = leaf.center().y - size_cell / 2;// Math.pow(2,block[i].coord.y);
			cz_min = leaf.center().z - size_cell / 2;// Math.pow(2,block[i].coord.z);

			cx_max = cx_min + size_cell;
			cy_max = cy_min + size_cell;
			cz_max = cz_min + size_cell;

			double xf, yf, zf, tx, ty, tz;



			if (ux > 0 && eyex <= cx_min - s && !is_blocked_x) {
				t = (cx_min - s - eyex) / ux;
				yf = eyey + t * uy;
				zf = eyez + t * uz;
				if (cy_min - s <= yf && yf <= cy_max + s
						&& cz_min - s * h <= zf && zf <= cz_max + h * s) {
					tx = Math.max(t - eps / ux, 0);
					if (tx == 0)
						is_blocked_x = true;
					dt = Math.min(dt, tx);

				}

			}
			if (ux < 0 && eyex >= cx_max + s && !is_blocked_x) {
				t = (cx_max + s - eyex) / ux;
				yf = eyey + t * uy;
				zf = eyez + t * uz;
				if (cy_min - s <= yf && yf <= cy_max + s
						&& cz_min - s * h <= zf && zf <= cz_max + h * s) {
					tx = Math.max(t + eps / ux, 0);
					if (tx == 0)
						is_blocked_x = true;
					dt = Math.min(dt, tx);

				}
			}
			if (uy > 0 && eyey <= cy_min - s && !is_blocked_y) {
				t = (cy_min - s - eyey) / uy;
				xf = eyex + t * ux;
				zf = eyez + t * uz;
				if (cx_min - s <= xf && xf <= cx_max + s
						&& cz_min - h * s <= zf && zf <= cz_max + h * s) {
					ty = Math.max(t - eps / uy, 0);
					if (ty == 0)
						is_blocked_y = true;
					dt = Math.min(dt, ty);
				}
			}
			if (uy < 0 && eyey >= cy_max + s && !is_blocked_y) {
				t = (cy_max + s - eyey) / uy;
				xf = eyex + t * ux;
				zf = eyez + t * uz;
				if (cx_min - s <= xf && xf <= cx_max + s
						&& cz_min - h * s <= zf && zf <= cz_max + h * s) {
					ty = Math.max(t + eps / uy, 0);
					if (ty == 0)
						is_blocked_y = true;
					dt = Math.min(dt, ty);
				}
			}
			if (uz > 0 && eyez <= cz_min - h * s && !is_blocked_z) {
				t = (cz_min - h * s - eyez) / uz;
				xf = eyex + t * ux;
				yf = eyey + t * uy;
				if (cy_min - s <= yf && yf <= cy_max + s && cx_min - s <= xf
						&& xf <= cx_max + s) {
					tz = Math.max(t - eps / uz, 0);
					if (tz == 0){
						is_blocked_z = true;
						shock_z=true;
					}
					dt = Math.min(dt, tz);
				}
			}
			if (uz < 0 && eyez >= cz_max + h * s && !is_blocked_z) {
				t = (cz_max + h * s - eyez) / uz;
				xf = eyex + t * ux;
				yf = eyey + t * uy;
				if (cy_min - s <= yf && yf <= cy_max + s && cx_min - s <= xf
						&& xf <= cx_max + s) {
					tz = Math.max(t + eps / uz, 0);
					if (tz == 0){
						is_blocked_z = true;
						shock_z=true;
					}
					dt = Math.min(dt, tz);
				}
			}
		}
		if (dt != 0) {
			eyex += ux * dt;
			eyey += uy * dt;
			eyez += uz * dt;
			position.x=eyex;
			position.y=eyey;
			position.z=eyez;
			velocity.x=ux;
			velocity.y=uy;
			velocity.z=uz;
			if (shock_z) {
				System.out.println(velocitynm1.toString());
				velocity.z=-shockCoeff*velocitynm1.z;
			}

		} else {
			velocity.x=ux;
			velocity.y=uy;
			velocity.z=uz;
			if (is_blocked_x)
				velocity.x = 0;
			if (is_blocked_y)
				velocity.y = 0;
			if (is_blocked_z)
				velocity.z = 0;

			position.x=eyex;
			position.y=eyey;
			position.z=eyez;
			avoid_blocks(listOfIntersectedLeaf, movingObject, dt_save,shock_z);
		}

	}
}
