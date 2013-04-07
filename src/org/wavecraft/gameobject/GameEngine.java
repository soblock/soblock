package org.wavecraft.gameobject;





import org.wavecraft.client.Timer;
import org.wavecraft.gameobject.physics.Physics;

import org.wavecraft.gameobject.physics.PhysicsWrapper;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.blocktree.modif.BlocktreeGrabber;
import org.wavecraft.geometry.blocktree.modif.ModifAdderBlocktree;
import org.wavecraft.geometry.blocktree.modif.ModifOctree;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderThreeDimFun;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderThreeDimFunModif;
import org.wavecraft.geometry.blocktree.BlocktreePriority;
import org.wavecraft.geometry.blocktree.BlocktreePriorityPosition;
import org.wavecraft.geometry.blocktree.BlocktreeRefiner;
import org.wavecraft.geometry.blocktree.BlocktreeUpdaterMaxPriority;
import org.wavecraft.geometry.blocktree.BlocktreeBuilder;

import org.wavecraft.geometry.blocktree.BlocktreeUpdater;
import org.wavecraft.geometry.blocktree.BlocktreeUpdaterSimple;
import org.wavecraft.geometry.blocktree.Terran;

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
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMenu;

import org.wavecraft.Soboutils.MathSoboutils;


// this class is the main game engine class
// it should not be aware of graphics or ui package and must
// not call any class or any method of these packages
// the only way to interact with the game engine is through events
// singleton
public class GameEngine implements UiEventListener{
	private static GameEngine gameEngine = null;
	private static Player player;
	private static Physics physicsPlayer;
	
	private static ModifOctree modif;
	
	private static Blocktree blocktree;
	private static BlocktreeUpdaterMaxPriority blockTreeUpdater;
	private static BlocktreeUpdaterSimple blockTreeUpdaterSimple;
	private static BlocktreeRefiner refiner;
	private static Thread refinerThread; 
	private static BlocktreeBuilder builder;
	
	public static int JMAX = 12;

	public static GameEngine getGameEngine(){
		if (gameEngine == null){
			gameEngine = new GameEngine();
		}
		return gameEngine;
	}

	

	public static BlocktreeBuilder getBlocktreeBuilder(){
		return builder;
	}

	public static Player getPlayer(){
		return player;
	}

	private GameEngine(){
		UiEventMediator.getUiEventMediator().addListener(this);
		initPlayer();
		
		new MathSoboutils();
		
		modif =new ModifOctree(0,0,0,JMAX,null,0.);



		WorldFunction wf = WorldFunctionBuilder.getWorldFunctionNoisyFlastNoisyContent(Math.pow(2, JMAX-1), 50, 10);		

		
		BlocktreePriorityPosition priority =  new BlocktreePriorityPosition();
		priority.setPosition(player.position);
		builder = new BlocktreeBuilderThreeDimFun(wf, priority);
	
		modif =new ModifOctree(0,0,0,10,null,0.);
		
		builder = new BlocktreeBuilderThreeDimFunModif(wf, priority, modif);

		blocktree = new Blocktree(0,0,0,JMAX);



		blocktree.setState(State.GRAND_FATHER);
		blockTreeUpdaterSimple = new BlocktreeUpdaterSimple(builder);
		blockTreeUpdaterSimple.init(blocktree);
 
		blockTreeUpdater = new BlocktreeUpdaterMaxPriority(builder);

		refiner = new BlocktreeRefiner();
		refinerThread = new Thread(refiner);
		refinerThread.start();

		

	}
	
	private void initPlayer(){
		player = new Player();
		player.position.x = Math.pow(2, JMAX-1);
		player.position.y = Math.pow(2, JMAX-1);
		player.position.z = Math.pow(2, JMAX-1)+100;
		// register main player to UiEvents
		// other player should NOT listen to UiEvents
		UiEventMediator.getUiEventMediator().addListener(player);
		physicsPlayer = new PhysicsWrapper();
	}
	
	private void initBlocktree(){
		
	}

	private static void startNewGame(){
		//physicsPlayer = new PhysicsWrapper();
		((PhysicsWrapper) physicsPlayer).switchPhys();
	}

	public static void update(){
		double dt = Timer.getDt();



		double t1 = System.currentTimeMillis();
		synchronized (GameEngine.class) {
			physicsPlayer.move(player, dt);
		}

		double dt_phys = System.currentTimeMillis()-t1;
		Profiler.getInstance().push("physicPlayer", dt_phys,Timer.getCurrT());



		
		// this function is being profiled in more detail


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


	public static Blocktree getBlocktree() {
		return blocktree;
	}


	public void prepareForExit(){
		refiner.setActive(false);

	}

	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventMenu){
			switch ((UiEventMenu ) e) {
			case START_NEW_GAME:
				startNewGame();
				break;

			default:
				break;
			}
		}
		
	}






}
