package org.wavecraft.gameobject;





import org.wavecraft.client.Timer;
import org.wavecraft.gameobject.physics.Physics;

import org.wavecraft.gameobject.physics.PhysicsWrapper;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderThreeDimFun;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderThreeDimFunModif;
import org.wavecraft.geometry.blocktree.BlocktreePriority;
import org.wavecraft.geometry.blocktree.BlocktreePriorityPosition;
import org.wavecraft.geometry.blocktree.BlocktreeRefiner;
import org.wavecraft.geometry.blocktree.BlocktreeUpdaterMaxPriority;
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
import org.wavecraft.geometry.worldfunction.ThreeDimContent;
import org.wavecraft.geometry.worldfunction.ThreeDimContentBiome;
import org.wavecraft.geometry.worldfunction.ThreeDimContentConstant;
import org.wavecraft.geometry.worldfunction.ThreeDimFunction;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionFlat;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionPerlin;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionPerlinMS;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionSphere;
import org.wavecraft.geometry.worldfunction.ThreeDimFunctionSum;
import org.wavecraft.geometry.worldfunction.WorldFunction;
import org.wavecraft.geometry.worldfunction.WorldFunctionBuilder;
import org.wavecraft.geometry.worldfunction.WorldFunctionWrapper;
import org.wavecraft.graphics.vbo.VBOBlocktreePool;
import org.wavecraft.stats.Profiler;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.modif.BlocktreeGrabber;
import org.wavecraft.modif.ModifAdder;
import org.wavecraft.modif.ModifAdderBlocktree;
import org.wavecraft.modif.ModifOctree;
import org.wavecraft.Soboutils.MathSoboutils;


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
	private static BlocktreeUpdaterMaxPriority blockTreeUpdater;
	private static BlocktreeUpdaterSimple blockTreeUpdaterSimple;
	private static BlocktreeRefiner refiner;
	private static Thread refinerThread; 
	private static BlocktreeBuilder builder;

	public static GameEngine getGameEngine(){
		if (gameEngine == null){
			gameEngine = new GameEngine();
		}
		return gameEngine;
	}

	public static OctreeBuilder getOctreeBuilder(){
		return octreeBuilder;
	}
	
	public static BlocktreeBuilder getBlocktreeBuilder(){
		return builder;
	}

	public static Player getPlayer(){
		return player;
	}

	private GameEngine(){
		player = new Player();


		player.position.x = 512;
		player.position.y = 512;
		player.position.z = 640;
		new MathSoboutils();

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



		WorldFunction wf = WorldFunctionBuilder.getWorldFunctionNoisyFlastNoisyContent(512, 512, 10);
		
		//wf = WorldFunctionBuilder.getWorldFunctionFlatUniform(0);
		
		
		ThreeDimFunction fun1 = new ThreeDimFunctionSphere(new Coord3d(1024, 1024, 1024), 1024);
		ThreeDimFunction fun2 = new ThreeDimFunctionPerlinMS();
		ThreeDimFunction fun = new ThreeDimFunctionSum(fun1, fun2);
		ThreeDimContent content = new ThreeDimContentBiome(-100, 2048, fun, 10);
		//wf = new WorldFunctionWrapper(content, fun);
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
		//BlocktreeBuilderAdapter blockTreeBuilder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandGeoCulling(4.1));
		//builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandGeoCulling(4.1));
		//WorldFunction wf2 = WorldFunctionBuilder.getWorldFunctionNoisyFlastNoisyContent(128,128);
		//BlocktreeBuilderAdapter blockTreeBuilder
		//builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getSincGeoCulling(new Coord3d(0, 0, 0), 100, 100, 100));
		 //wf = new WorldFunctionWrapper(new ThreeDimContentConstant(), new ThreeDimFunctionFlat(1));
		//wf = new WorldFunctionWrapper(new ThreeDimContentConstant(), new ThreeDimFunctionPerlin());
		OctreeBuilder ob2 = OctreeBuilderBuilder.getBuilder(wf);
		builder = new BlocktreeBuilderAdapter(ob2);
		//
		BlocktreePriorityPosition priority =  new BlocktreePriorityPosition();
		priority.setPosition(player.position);
		builder = new BlocktreeBuilderThreeDimFun(wf, priority);
		//ModifOctree modif = new ModifOctree(0, 0, 0, 10, 0, 0);
		modif =new ModifOctree(0,0,0,10,5,0.);
		modif.computeBounds();
		modif.sumAncestors = 0;
		modif.computeSumAncestors();

		builder = new BlocktreeBuilderThreeDimFunModif(wf, priority, modif);
		
		//builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandGeoCulling(0.1));
		//builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getSphereGeoCullin(new Coord3d(512, 512, 512), 500));
		
		
		
//	builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getPerlinMSGeoCulling());

		blocktree = new Blocktree(0,0,0,10);

		
		
		blocktree.setState(State.GRAND_FATHER);
		blockTreeUpdaterSimple = new BlocktreeUpdaterSimple(builder);
		blockTreeUpdaterSimple.init(blocktree);


		//blockTreeUpdater = new BlocktreeUpdaterSimple(blocktree, blockTreeBuilder); 
		blockTreeUpdater = new BlocktreeUpdaterMaxPriority(builder);

		refiner = new BlocktreeRefiner();
		refinerThread = new Thread(refiner);
		refinerThread.start();

		//((BlocktreeUpdaterSimple) blockTreeUpdater).init(blocktree);
		//((BlockTreeUpdaterMaxPriority) blockTreeUpdater).updateANode(blocktree);
		//blockTreeUpdater.update(blocktree);


		//		Thread updateThread = new Thread(new Runnable() {
		//			@Override
		//			public void run() {
		//				while (true){
		//					((BlockTreeUpdaterMaxPriority) blockTreeUpdater).updateANode(blocktree);
		//				}
		//			}
		//		});



		//updateThread.start();
		
		
	}

	public static void update(){
		double dt = Timer.getDt();



		double t1 = System.currentTimeMillis();
		synchronized (GameEngine.class) {
			physicsPlayer.move(player, dt);
		}

		double dt_phys = System.currentTimeMillis()-t1;
		Profiler.getInstance().push("physicPlayer", dt_phys,Timer.getCurrT());



		double t2 = System.currentTimeMillis();
		if (Timer.getNframe()%1 == 0){
			octreeUpdater.updateOctree();
		}
		// this function is being profiled in more detail
		double dt_octreeUpdater = System.currentTimeMillis()-t2;
		//Profiler.getInstance().push("updateOctree", dt_octreeUpdater,Timer.getCurrT());

		//Octree nearest = BlockGrabber.nearestIntersectedLeaf(GameEngine.getOctree(), GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		//Blocktree nearest = BlocktreeGrabber.nearestIntersectedLeaf(blocktree,player.position, player.getVectorOfSight());
		Octree obstacle=new Octree(new DyadicBlock(0, 0, 0, Octree.JMAX), null);
		obstacle.initSon(2);
		Octree son1= (obstacle.getSons())[2];
		son1.initSon(1);

		//water.moveFluid(octree,player.position,octreeBuilder);

		

		ModifAdderBlocktree.setModif(modif);
		ModifAdderBlocktree.setOctree(blocktree);

		refiner.setBuilder(builder);

		if (true){

			// the refiner has finished, copy the results in the current tree.
			if (refiner.getState() == BlocktreeRefiner.State.FINISHED){
				refiner.doInMainThreadWhenDone();
				Blocktree nodeToCopy = refiner.getNodeToRefine();
				if (nodeToCopy.getJ() == blocktree.getJ()){
					blocktree = nodeToCopy;
				} else {
					nodeToCopy.becomeSonOfMyFather();
				}
				refiner.setState(BlocktreeRefiner.State.NO_JOB);
			}


			// the refiner has nothing to do : give him a new job
			if (refiner.getState() ==  BlocktreeRefiner.State.NO_JOB){
				Blocktree.State nextUpdateState;
				if (Math.random()>0.5){
					nextUpdateState = State.GRAND_FATHER;
				}
				else {
					nextUpdateState = State.PATRIARCH;
				}
				Blocktree nodeToUpdate = blockTreeUpdater.getArgMaxPriorityPerState(blocktree, nextUpdateState);
				Blocktree nodeToRecompute = ModifAdderBlocktree.pushRecompute();
				if (nodeToRecompute!=null){
					nodeToUpdate = nodeToRecompute;
					refiner.setRegenate(true);
					//nodeToUpdate.setState(State.PATRIARCH);
				} else {
					refiner.setRegenate(false);
				}
				//System.out.println("next state tu update " + nextUpdateState +"node to update " + nodeToUpdate);
				if (nodeToUpdate!=null){
					
					refiner.setNodeToRefine(nodeToUpdate);
					refiner.setBuilder(builder);
					refiner.setState(BlocktreeRefiner.State.READY_TO_PROCESS_JOB);
				}
			}
		}
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

	
	public void prepareForExit(){
		refiner.setActive(false);
		
	}






}
