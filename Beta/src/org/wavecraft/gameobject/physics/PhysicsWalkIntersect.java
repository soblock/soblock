package org.wavecraft.gameobject.physics;

//import Coord3d;


import org.wavecraft.Soboutils.MathSoboutils;


import java.util.EnumSet;
import java.util.List;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.gameobject.GameObjectMoving;
import org.wavecraft.gameobject.GameObjectMovingOriented;
import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Blocktree;
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
	double bounceCoeff=0.3;
	double VminBounce=2;

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
		UiEventMediator.getUiEventMediator().addListener(this);
	}


	public void getTargetVelocity(GameObjectMoving movingObject){
		if (movingObject instanceof GameObjectMovingOriented){
			double theta = ((GameObjectMovingOriented) movingObject).getTheta();
			double phi = ((GameObjectMovingOriented) movingObject).getPhi();
			double ux = (Math.cos(theta) * Math.cos(phi));
			double uy = (Math.sin(theta) * Math.cos(phi));
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
			//Octree root = GameEngine.getOctree();
			Blocktree root = GameEngine.getBlocktree();
			BoundingBox box = movingObject.getTranslatedBoundingBox();
			box= box.extrude(velocity.scalarMult(dt));
			List<Blocktree> listOfIntersectedLeaf = CollisionDetectionBlocktree.intersectedLeaf(root, box);
			avoid_blocks(listOfIntersectedLeaf, movingObject, dt);
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

	public void avoid_blocks(List<? extends DyadicBlock> listOfIntersectedLeaf,GameObjectMoving movingObject, double dt){
		boolean shock_zp=false;
		boolean shock_zm=false;
		velocitynm1= new Coord3d(velocity);
		avoid_blocks(listOfIntersectedLeaf, movingObject, dt,shock_zp,shock_zm);

	}

	public void avoid_blocks(List<? extends DyadicBlock> listOfIntersectedLeaf,GameObjectMoving movingObject, double dt,boolean shock_zp, boolean shock_zm) {
		// / the player is a considered a sphere of radius size_player
		double playerWidth = 0.15; 
		double playerHeightDown = 1.5; // under-the-eye player size
		double playerHeightUp = 0.30; // up-ther eye player size
		double size_cell;
		double dt_save = dt;
		double cx_min, cx_max, cy_min, cy_max, cz_min, cz_max, t;
		boolean is_blocked_x = false;
		boolean is_blocked_y = false;
		boolean is_blocked_z = false;
		// zero tolerance for dt
		double eps_dt=1E-5;

		// double precision for the velocity
		double eps_vel=1E-5;
		if (Math.abs(velocity.x)<eps_vel) velocity.x=0.;
		if (Math.abs(velocity.y)<eps_vel) velocity.y=0.;
		if (Math.abs(velocity.z)<eps_vel) velocity.z=0.;
		double ux = velocity.x;
		double uy = velocity.y;
		double uz = velocity.z;


		//	double dxnorm=Math.sqrt(velocity.x*velocity.x+velocity.y*velocity.y+velocity.z*velocity.z)*dt;

		Coord3d position=movingObject.getPosition();
		double eyex = position.x;
		double eyey = position.y;
		double eyez = position.z;

		double eps = 1E-3;//0.01;//dxnorm/dt;// eyez+=speed;





		for (int i = 0; i < listOfIntersectedLeaf.size(); i++) {
			DyadicBlock leaf = listOfIntersectedLeaf.get(i);
			size_cell = MathSoboutils.dpowerOf2[leaf.getJ()];

			// bb of current cell
			cx_min = leaf.center().x - size_cell / 2;// Math.pow(2,block[i].coord.x);
			cy_min = leaf.center().y - size_cell / 2;// Math.pow(2,block[i].coord.y);
			cz_min = leaf.center().z - size_cell / 2;// Math.pow(2,block[i].coord.z);

			cx_max = cx_min + size_cell;
			cy_max = cy_min + size_cell;
			cz_max = cz_min + size_cell;

			double xf, yf, zf, tx, ty, tz;



			if (ux > eps_vel && eyex <= cx_min - playerWidth && !is_blocked_x) {
				t = (cx_min - playerWidth - eyex) / ux;
				yf = eyey + t * uy;
				zf = eyez + t * uz;
				if (cy_min - playerWidth <= yf && yf <= cy_max + playerWidth
						&& cz_min - playerHeightUp <= zf && zf <= cz_max + playerHeightDown) {
					tx = Math.max(t - eps / ux, 0);
					if (tx <eps_dt)
						is_blocked_x = true;
					dt = Math.min(dt, tx);

				}

			}
			if (ux < -eps_vel && eyex >= cx_max + playerWidth && !is_blocked_x) {
				t = (cx_max + playerWidth - eyex) / ux;
				yf = eyey + t * uy;
				zf = eyez + t * uz;
				if (cy_min - playerWidth <= yf && yf <= cy_max + playerWidth
						&& cz_min - playerHeightUp <= zf && zf <= cz_max + playerHeightDown) {
					tx = Math.max(t + eps / ux, 0);
					if (tx <eps_dt)
						is_blocked_x = true;
					dt = Math.min(dt, tx);

				}
			}
			if (uy > eps_vel && eyey <= cy_min - playerWidth && !is_blocked_y) {
				t = (cy_min - playerWidth - eyey) / uy;
				xf = eyex + t * ux;
				zf = eyez + t * uz;
				if (cx_min - playerWidth <= xf && xf <= cx_max + playerWidth
						&& cz_min - playerHeightUp <= zf && zf <= cz_max + playerHeightDown) {
					ty = Math.max(t - eps / uy, 0);
					if (ty <eps_dt)
						is_blocked_y = true;
					dt = Math.min(dt, ty);
				}
			}
			if (uy < -eps_vel && eyey >= cy_max + playerWidth && !is_blocked_y) {
				t = (cy_max + playerWidth - eyey) / uy;
				xf = eyex + t * ux;
				zf = eyez + t * uz;
				if (cx_min - playerWidth <= xf && xf <= cx_max + playerWidth
						&& cz_min - playerHeightUp <= zf && zf <= cz_max + playerHeightDown) {
					ty = Math.max(t + eps / uy, 0);
					if (ty <eps_dt)
						is_blocked_y = true;
					dt = Math.min(dt, ty);
				}
			}
			if (uz > eps_vel && eyez <= cz_min - playerHeightUp && !is_blocked_z) {
				t = (cz_min - playerHeightUp - eyez) / uz;
				xf = eyex + t * ux;
				yf = eyey + t * uy;
				if (cy_min - playerWidth <= yf && yf <= cy_max + playerWidth && cx_min - playerWidth <= xf
						&& xf <= cx_max + playerWidth) {
					tz = Math.max(t - eps / uz, 0);
					if (tz <eps_dt){
						is_blocked_z = true;
						shock_zp=true;
					}
					dt = Math.min(dt, tz);
				}
			}
			if (uz < -eps_vel && eyez >= cz_max + playerHeightDown && !is_blocked_z) {
				t = (cz_max + playerHeightDown - eyez) / uz;
				xf = eyex + t * ux;
				yf = eyey + t * uy;
				if (cy_min - playerWidth <= yf && yf <= cy_max + playerWidth && cx_min - playerWidth <= xf
						&& xf <= cx_max + playerWidth) {
					tz = Math.max(t + eps / uz, 0);
					if (tz <eps_dt){
						is_blocked_z = true;
						shock_zm=true;
					}
					dt = Math.min(dt, tz);
				}
			}
		}
		if (Math.abs(dt)> eps_dt) {
					
			position.x += ux * dt;
			position.y += uy * dt;
			position.z += uz * dt;
			velocity.x=ux;
			velocity.y=uy;
			velocity.z=uz;
			if (shock_zp) {
				//System.out.println(velocitynm1.toString());
				velocity.z=-shockCoeff*velocitynm1.z;
			}
			if (shock_zm && VminBounce<-uz){
				//System.out.println(velocity.toString());
				velocity.z=-bounceCoeff*velocitynm1.z;
				//System.out.println(velocity.toString());
			}

		} else {
			
			if (is_blocked_x){
				//System.out.printf("XXX \n");
				velocity.x = 0;
			}
			if (is_blocked_y){
				//System.out.printf("YYY \n");
				velocity.y = 0;
			}
			if (is_blocked_z){
				//System.out.printf("ZZZ	 \n");
				velocity.z = 0;
			}

			if(!(is_blocked_x && is_blocked_y && is_blocked_z)){
				avoid_blocks(listOfIntersectedLeaf, movingObject, dt_save,shock_zp,shock_zm);
			}
		}

	}
}
