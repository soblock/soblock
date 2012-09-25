package org.wavecraft.gameobject;



import org.wavecraft.client.Timer;
import org.wavecraft.gameobject.physics.Physics;
import org.wavecraft.gameobject.physics.PhysicsFreeFlight;
import org.wavecraft.gameobject.physics.PhysicsWrapper;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeState;
import org.wavecraft.geometry.octree.OctreeUtils;
import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.builder.OctreeBuilderBuilder;
import org.wavecraft.geometry.octree.builder.OctreeUpdater;
import org.wavecraft.geometry.octree.builder.OctreeUpdaterPartial;
import org.wavecraft.geometry.octree.builder.OctreeUpdaterPriority;
import org.wavecraft.geometry.octree.events.OctreeEventListenerBasic;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionNoisyFlat;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionFlat;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionSinc;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionSphere;
import org.wavecraft.geometry.worldfunction.WorldFunction;
import org.wavecraft.geometry.worldfunction.WorldFunctionBuilder;
import org.wavecraft.stats.Profiler;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.modif.BlockGrabber;
import org.wavecraft.modif.ModifOctree;
import org.wavecraft.Soboutils.Math_Soboutils;


// this class is the main game engine class
// it should not be aware of graphics or ui package and must
// not call any class or any method of these packages
// the only way to interact with the game engine is through events
// singleton
public class GameEngine {
	private static GameEngine gameEngine = null;
	private static Player player;
	private static Physics physicsPlayer;
	private static Octree octree;
	private static ModifOctree modif;
	//private static Octree octreeLastUpdatePosition;
	private static OctreeBuilder octreeBuilder;
	private static OctreeUpdater octreeUpdater;

	public static GameEngine getGameEngine(){
		if (gameEngine == null){
			gameEngine = new GameEngine();
		}
		return gameEngine;
	}
	
	public static OctreeBuilder getOctreeBuilder(){
		return octreeBuilder;
	}

	public static Player getPlayer(){
		return player;
	}

	private GameEngine(){
		player = new Player();
		player.position.x=95;
		player.position.y=91;
		player.position.z=240;

		new Math_Soboutils();
		
		// register main player to UiEvents
		// other player should NOT listen to UiEvents
		UiEventMediator.addListener(player);
		//physicsPlayer = new PhysicsFreeFlight();
		physicsPlayer = new PhysicsWrapper();

		octree = new Octree(new DyadicBlock(0, 0, 0, Octree.JMAX), null);
		modif =new ModifOctree(0,0,0,Octree.JMAX,5,0.);
//		ModifOctree son = new ModifOctree(modif.subBlock(4).x, 
//				modif.subBlock(4).y, 
//				modif.subBlock(4).z,Octree.JMAX-1,5,100000);
//		ModifOctree son1 = new ModifOctree(son.subBlock(4).x, 
//				son.subBlock(4).y, 
//				son.subBlock(4).z,Octree.JMAX-2,5,-200000);
//		ModifOctree son2 = new ModifOctree(son.subBlock(4).x, 
//				son.subBlock(4).y, 
//				son.subBlock(4).z,Octree.JMAX-3,5, 200000);
//		ModifOctree son3 = new ModifOctree(son.subBlock(4).x, 
//				son.subBlock(4).y, 
//				son.subBlock(4).z,Octree.JMAX-4,5, -200000);
//		ModifOctree son4 = new ModifOctree(son.subBlock(4).x, 
//				son.subBlock(4).y, 
//				son.subBlock(4).z,Octree.JMAX-5,5, 200000);
//		son4.father=son3;
//		son3.father=son2;
//		son2.father=son1;
//		son1.father=son;
//		son.father=modif;
//		son3.sons=new ModifOctree [8];
//		son2.sons=new ModifOctree [8];
//		son1.sons=new ModifOctree [8];
//		son.sons= new ModifOctree [8];
//		modif.sons= new ModifOctree [8];
//		son3.sons[4]=son4;
//		son2.sons[4]=son3;
//		son1.sons[4]=son2;
//		son.sons[4]=son1;
//		modif.sons[4]=son;
		
		
		
		modif.computeBounds();
		modif.sumAncestors = 0;
		modif.computeSumAncestors();
		//Octree son1 = new Octree(son.coord.subCoord(7),Octree.JMAX-2,4);




		//octreeBuilder = OctreeBuilderBuilder.getFlatlandGeoCulling(0.5);
		//octreeBuilder = OctreeBuilderBuilder.getSphereNoculling(new Coord3d(50, 50, 50), 50);
		//octreeBuilder = OctreeBuilderBuilder.getSphereGeoCullin(new Coord3d(600, 600, 600), 500);
		////	octreeBuilder = OctreeBuilderBuilder.getSphereGeoCullin(new Coord3d(16, 1, 1), 4);
		//				Coord3d center = new Coord3d(0,0,0);
		//				double scale = 40;
		//				double deltaz = 200;
		//				double Z0 = 10;
		//octreeBuilder = OctreeBuilderBuilder.getSincNoCulling(center, scale, deltaz, Z0);
		//octreeBuilder = OctreeBuilderBuilder.getSincGeoCulling(center, scale, deltaz, Z0);
		//octreeBuilder = OctreeBuilderBuilder.getPerlinGeoCulling();
		//octreeBuilder = OctreeBuilderBuilder.getPerlinMSGeoCulling();
		double z0=1.5*Math_Soboutils.powerOf2[Octree.JMAX-1];
		
		octreeBuilder = OctreeBuilderBuilder.getGeoCullingUniformFromThreeDimFunctionWithModif(new ThreeDimFunctionNoisyFlat(z0),modif);
		double zmax =z0 + 100;
		WorldFunction wf = WorldFunctionBuilder.getWorldFunctionNoisyFlastNoisyContent(z0,zmax);
		octreeBuilder = OctreeBuilderBuilder.getBuilderModif(wf, modif);
		//octreeBuilder = OctreeBuilderBuilder.getGeoCullingUniformFromThreeDimFunctionWithModif(new ThreeDimFunctionFlat(Math.pow(2, Octree.JMAX-1)),modif);
		//octreeBuilder = OctreeBuilderBuilder.getGeoCullingUniformFromThreeDimFunctionWithModif(new ThreeDimFunctionSinc(new Coord3d(512, 512, 512), 100, 100, 256),modif);
		//octreeBuilder = OctreeBuilderBuilder.getGeoCullingUniformFromThreeDimFunctionWithModif(new ThreeDimFunctionSphere(new Coord3d(256, 256, 256), 256),modif);
		
		OctreeEventMediator.getInstance();
		OctreeEventListenerBasic oelb = new OctreeEventListenerBasic();
		OctreeEventMediator.addListener(oelb);
		//octreeUpdater = new OctreeUpdaterPartial(octree,octreeBuilder);
		octreeUpdater = new OctreeUpdaterPriority(octree, octreeBuilder);
	}

	public static void update(){
		double dt = Timer.getDt();
		
		
		
		double t1 = System.currentTimeMillis();
		physicsPlayer.move(player, dt);
		double dt_phys = System.currentTimeMillis()-t1;
		Profiler.getInstance().push("physicPlayer", dt_phys,Timer.getCurrT());


		
		//double t2 = System.currentTimeMillis();
		if (Timer.getNframe()%1 == 0){
			octreeUpdater.updateOctree();
		}
		// this function is being profiled in more detail
		//double dt_octreeUpdater = System.currentTimeMillis()-t2;
		//Profiler.getInstance().push("updateOctree", dt_octreeUpdater,Timer.getCurrT());
		
		//Octree nearest = BlockGrabber.nearestIntersectedLeaf(GameEngine.getOctree(), GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
	

	}

	public static Octree getOctree(){
		return octree;
	}


}
