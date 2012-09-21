package org.wavecraft.modif;

import org.wavecraft.Soboutils.Math_Soboutils;
import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeState;
import org.wavecraft.geometry.octree.OctreeStateDead;
import org.wavecraft.geometry.octree.OctreeStateFatherCool;
import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.OctreeUtils;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.ui.KeyboardBinding;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMouseClicked;


// singleton 
public class ModifAdder implements UiEventListener {

	private static ModifOctree modif ;
	private static Octree octree;
	private static int targetContent = 1;
	private static int targetAddJ = 1; // the size of the modif
	private static int targetRemoveJ = 2; // the size of the modif
	private static boolean whenLeftClickAddBlock = true;

	public static void setTargetContent(int content){
		targetContent = content;
	}


	public static DyadicBlock getNodeToRemove(){
		// find nearest neighbor 
		Octree best = BlockGrabber.nearestIntersectedLeaf(octree, GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		if (best == null){return null;}
		// find best face
		Face face = best.nearestIntersectedFace(GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		DyadicBlock nodeToRemove = face.getFather();
		nodeToRemove = nodeToRemove.ancestor(targetRemoveJ);
		return nodeToRemove;
	}

	public static DyadicBlock getNodeToAdd(){
		// find nearest neighbor 
		Octree best = BlockGrabber.nearestIntersectedLeaf(octree, GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		if (best == null){return null;}
		// find best face
		Face face = best.nearestIntersectedFace(GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		DyadicBlock nodeToAdd = face.getNeighbor();
		nodeToAdd = nodeToAdd.ancestor(targetAddJ);
		return nodeToAdd;
	}

	private static ModifAdder instance;
	public static ModifAdder getInstance(){
		if (instance == null){
			instance = new ModifAdder();
		}
		return instance;
	}

	private ModifAdder(){
	}


	public static void setOctree(Octree octree){
		getInstance();
		ModifAdder.octree = octree;
	}

	public static void setModif(ModifOctree modif){
		getInstance();
		ModifAdder.modif = modif;
	}


	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventMouseClicked && whenLeftClickAddBlock){
			UiEventMouseClicked emc = (UiEventMouseClicked) (e);
			if (emc.isButtonPressed){
				if (emc.buttonId == 0 ){
					DyadicBlock nodeToAdd = getNodeToAdd();
					if (nodeToAdd !=null){
						addBlock(nodeToAdd, targetContent);
					}
				}
				if (emc.buttonId == 1 ){
					DyadicBlock nodeToRemove = getNodeToRemove();
					if (nodeToRemove != null){	
						removeBlock(nodeToRemove);
					}
				}
			}
		}
		if (e instanceof UiEventKeyboardPressed){
			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_GAME_ADDBLOCK){
				DyadicBlock nodeToAdd = getNodeToAdd();
				if (nodeToAdd !=null){
					addBlock(nodeToAdd, targetContent);
				}
			}

			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_GAME_KILLBLOCK){
				DyadicBlock nodeToRemove = getNodeToRemove();
				if (nodeToRemove != null){
					System.out.println("node to remove : " + nodeToRemove.toString());
					removeBlock(nodeToRemove);
				}
			}

			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_MODIF_INC_ADD){
				targetAddJ++;
				targetAddJ = Math.min(Octree.JMAX, Math.max(0, targetAddJ));
			}

			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_MODIF_DEC_ADD){
				targetAddJ--;
				targetAddJ = Math.min(Octree.JMAX, Math.max(0, targetAddJ));
			}

			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_MODIF_INC_REMOVE){
				targetRemoveJ++;
				targetRemoveJ = Math.min(Octree.JMAX, Math.max(0, targetRemoveJ));
			}

			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_MODIF_DEC_REMOVE){
				targetRemoveJ--;
				targetRemoveJ = Math.min(Octree.JMAX, Math.max(0, targetRemoveJ));
			}



		}

	}

	private void addBlock(DyadicBlock nodeToAdd, int content){
		double value = -1E20;

		modif.addModif(nodeToAdd, value, content);
		modif.computeBounds();
		
		Octree nodeToRegenerate = octree.smallestCellContaining(nodeToAdd);
		// if the smallest node is larger than the node to add,
		// add sons quietly and iterate
		while (nodeToRegenerate.getJ() != nodeToAdd.getJ()){
			nodeToRegenerate.setState(OctreeStateFatherCool.getInstance());
			nodeToRegenerate.initSonsQuietly();
			nodeToRegenerate =  nodeToRegenerate.smallestCellContaining(nodeToAdd);
			nodeToRegenerate.setState(OctreeStateFatherCool.getInstance());
		}

		Octree fatherFucker = nodeToRegenerate.getFather();
		while(fatherFucker.getJ()<Octree.JMAX){
			fatherFucker.setState(OctreeStateFatherCool.getInstance());
			fatherFucker = fatherFucker.getFather();
		}
		nodeToRegenerate.setContent(content);
		nodeToRegenerate.setState(OctreeStateLeaf.getInstance());
		OctreeEvent event = new OctreeEvent(nodeToRegenerate, OctreeEventKindof.LEAFY);
		OctreeEventMediator.addEvent(event);
	}

	private void removeBlock(DyadicBlock nodeToRemove){
		// add modif to to modif save octree
		double value = 1E20;
		modif.addModif(nodeToRemove, value, 0);
		modif.computeBounds();
		
		// leafy every non null neighbor
		DyadicBlock[] neighbors = nodeToRemove.sixNeighbors();
		for (int i = 0;i<6; i++){
			if (neighbors[i]!=null){
				//System.out.println("neigbors : "+ neighbors[i].toString());
				Octree octreeNeighbor = octree.smallestCellContaining(neighbors[i]);
				if (!(octreeNeighbor.getState() instanceof OctreeStateDead)){ 
					OctreeEvent event = new OctreeEvent(octreeNeighbor, OctreeEventKindof.LEAFY);
					OctreeEventMediator.addEvent(event);
				}
			}
		}

		// remove cell
		Octree octreeToRemove = octree.smallestCellContaining(nodeToRemove);
		OctreeEvent event = new OctreeEvent(octreeToRemove, OctreeEventKindof.KILL);
		OctreeEventMediator.addEvent(event);
	}


}
