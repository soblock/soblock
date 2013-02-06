package org.wavecraft.gameobject;



import org.wavecraft.client.Timer;
import org.wavecraft.gameobject.physics.Physics;

import org.wavecraft.gameobject.physics.PhysicsWrapper;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.blocktree.BlocktreeBuilder;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderAdapter;
import org.wavecraft.geometry.blocktree.BlocktreeUpdater;
import org.wavecraft.geometry.blocktree.BlocktreeUpdaterSimple;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.builder.OctreeBuilderBuilder;
import org.wavecraft.geometry.octree.builder.OctreeUpdater;
import org.wavecraft.geometry.octree.builder.OctreeUpdaterPriority;
import org.wavecraft.geometry.octree.events.OctreeEventListenerBasic;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.geometry.octree.fluid.FluidTree;
import org.wavecraft.geometry.worldfunction.WorldFunction;
import org.wavecraft.geometry.worldfunction.WorldFunctionBuilder;
import org.wavecraft.stats.Profiler;
import org.wavecraft.ui.events.UiEventMediator;
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
	private static FluidTree water;
	private static ModifOctree modif;
	private static OctreeBuilder octreeBuilder;
	private static OctreeUpdater octreeUpdater;
	
	private static Blocktree blocktree;
	private static BlocktreeUpdater blockTreeUpdater;

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

		player.position.x=Math.pow(2,Octree.JMAX-1);//95;
		player.position.y=Math.pow(2,Octree.JMAX-1);
		player.position.z=Math.pow(2,Octree.JMAX);
		
		if (Octree.JMAX == 10){
			player.position.x = 450;
			player.position.x = 380;
			player.position.z = 816;
		}

		new Math_Soboutils();
		
		// register main player to UiEvents
		// other player should NOT listen to UiEvents
		UiEventMediator.addListener(player);
		//physicsPlayer = new PhysicsFreeFlight();
		physicsPlayer = new PhysicsWrapper();

		octree = new Octree(new DyadicBlock(0, 0, 0, Octree.JMAX), null);
		modif =new ModifOctree(0,0,0,Octree.JMAX,5,0.);

		
		
		modif.computeBounds();
		modif.sumAncestors = 0;
		modif.computeSumAncestors();


		double z0=Math_Soboutils.powerOf2[Octree.JMAX]/2;

		WorldFunction wf = WorldFunctionBuilder.getWorldFunctionNoisyFlastNoisyContent(z0,z0);
		octreeBuilder = OctreeBuilderBuilder.getBuilderModif(wf, modif);
		
		OctreeEventMediator.getInstance();
		OctreeEventListenerBasic oelb = new OctreeEventListenerBasic();
		OctreeEventMediator.addListener(oelb);
		//octreeUpdater = new OctreeUpdaterPartial(octree,octreeBuilder);
		octreeUpdater = new OctreeUpdaterPriority(octree, octreeBuilder);
		
		//water = new FluidTree(0,0,0,Octree.JMAX,4);
		//water.initSon(6);
		//water.initializeVolumes();
		

		//blockTreeUpdater = new BOctreeBuilderBuilder.getFlatlandGeoCulling(z0);
		//BlocktreeBuilderAdapter blockTreeBuilder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandNoculling(0.1));
		//BlocktreeBuilderAdapter blockTreeBuilder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandGeoCulling(16.1));
		WorldFunction wf2 = WorldFunctionBuilder.getWorldFunctionNoisyFlastNoisyContent(128,128);
		BlocktreeBuilderAdapter blockTreeBuilder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getSincGeoCulling(new Coord3d(0, 0, 0), 100, 100, 10));
		
		blocktree = new Blocktree(0,0,0,10);
		blocktree.setState(State.GRAND_FATHER);
		blockTreeUpdater = new BlocktreeUpdaterSimple(blocktree, blockTreeBuilder); 
		((BlocktreeUpdaterSimple) blockTreeUpdater).init();
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
		Octree obstacle=new Octree(new DyadicBlock(0, 0, 0, Octree.JMAX), null);
		obstacle.initSon(2);
		Octree son1= (obstacle.getSons())[2];
		son1.initSon(1);
		
		//water.moveFluid(octree,player.position,octreeBuilder);
		
		blockTreeUpdater.update();
	}

	public static Octree getOctree(){
		return octree;
	}
	public static FluidTree getWater(){
		return water;
	}

	public static Blocktree getBlocktree() {
		return blocktree;
	}

	



}
