package org.wavecraft.modif;

import java.util.ArrayList;
import java.util.List;


import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeStateFatherCool;
import org.wavecraft.geometry.octree.OctreeStateGround;
import org.wavecraft.geometry.octree.OctreeStateLeaf;
import org.wavecraft.geometry.octree.OctreeStateNotYetVisited;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventKindof;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;
import org.wavecraft.ui.KeyboardBinding;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMouseClicked;
import org.wavecraft.ui.menu.Console;


// singleton 
public class ModifAdderBlocktree implements UiEventListener {

	private static ModifOctree modif ;
	private static Blocktree root;
	private static int targetContent = 1;
	private static int targetJ = 1; // the size of the modif
	private static final int MAX_TARGET_J = 10;
	private static ModifAdderBlocktree instance;
	private static boolean whenLeftClickAddBlock = true;
	
	private static List<Blocktree> toRecomputeVBO = null; 

	public static void setTargetContent(int content){
		targetContent = content;
	}


	
	
	public static Blocktree pushRecompute(){
		if (toRecomputeVBO.isEmpty()){
			return null;
		}
		else {
			Blocktree toRecompute = toRecomputeVBO.get(toRecomputeVBO.size()-1);
			toRecomputeVBO.remove(toRecomputeVBO.size()-1);
			return toRecompute;
		}
	}


	public static ModifAdderBlocktree getInstance(){
		if (instance == null){
			instance = new ModifAdderBlocktree();
		}
		return instance;
	}

	private ModifAdderBlocktree(){
		UiEventMediator.addListener(this);
		toRecomputeVBO = new ArrayList<Blocktree>();
	}


	public static DyadicBlock getNodeToRemove(){
		// find nearest neighbor 
		Blocktree best = BlocktreeGrabber.nearestIntersectedLeaf(root, GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		if (best == null){return null;}
		// find best face
		Face face = best.nearestIntersectedFace(GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		DyadicBlock nodeToRemove = face.getFather();
		nodeToRemove = nodeToRemove.ancestor(targetJ);
		return nodeToRemove;
	}

	public static DyadicBlock getNodeToAdd(){
		// find nearest neighbor 
		Blocktree best = BlocktreeGrabber.nearestIntersectedLeaf(root, GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		if (best == null){return null;}
		// find best face
		Face face = best.nearestIntersectedFace(GameEngine.getPlayer().getPosition(), GameEngine.getPlayer().getVectorOfSight());
		DyadicBlock nodeToAdd = face.getBlockInFrontOf();
		nodeToAdd = nodeToAdd.ancestor(targetJ);
		return nodeToAdd;
	}


	public static void setOctree(Blocktree octree){
		getInstance();
		ModifAdderBlocktree.root = octree;
	}

	public static void setModif(ModifOctree modif){
		getInstance();
		ModifAdderBlocktree.modif = modif;
	}

	private void addBlock(DyadicBlock nodeToAdd, int content){
		// modify the modif tree properly
		double value = -1E16*(1 + Octree.JMAX - nodeToAdd.getJ());
		// it is crucial to make the value vary with J so that recursive 
		// modif handle the content properly
		modif.addModif(nodeToAdd, value, content);
		modif.computeBounds();
		
		// find the smallest grand father or patriarch to repush to GC
		Blocktree nodeToRecomputeVBO = root.smallestPatriarchOrGrandFatherContaining(nodeToAdd);
		System.out.println(nodeToRecomputeVBO);
		toRecomputeVBO.add(nodeToRecomputeVBO);
		
		
	}
	
	
	
//
//	private void addBlock(DyadicBlock nodeToAdd, int content){
//		// modify the modif tree properly
//		double value = -1E16*(1 + Octree.JMAX - nodeToAdd.getJ());
//		// it is crucial to make the value vary with J so that recursive 
//		// modif handle the content properly
//		modif.addModif(nodeToAdd, value, content);
//		modif.computeBounds();
//
//		// regenerate relevant nodes in the terran tree
//		Octree nodeToRegenerate = octree.smallestCellContaining(nodeToAdd);
//		// if the smallest node is larger than the node to add,
//		// add sons quietly and iterate
//		while (nodeToRegenerate.getJ() != nodeToAdd.getJ()){
//			nodeToRegenerate.setState(OctreeStateFatherCool.getInstance());
//			nodeToRegenerate.initSonsQuietly();
//			nodeToRegenerate =  nodeToRegenerate.smallestCellContaining(nodeToAdd);
//			nodeToRegenerate.setState(OctreeStateFatherCool.getInstance());
//		}
//
//		Octree fatherBeCool = nodeToRegenerate.getFather();
//		while(fatherBeCool.getJ()<Octree.JMAX){
//			fatherBeCool.setState(OctreeStateFatherCool.getInstance());
//			fatherBeCool = fatherBeCool.getFather();
//		}
//		nodeToRegenerate.setContent(content);
//		nodeToRegenerate.setState(OctreeStateLeaf.getInstance());
//		OctreeEvent event = new OctreeEvent(nodeToRegenerate, OctreeEventKindof.LEAFY);
//		OctreeEventMediator.addEvent(event);
//		
//		// revisit adjacent leaf cell (they might be useless now)
//		ArrayList<Octree> adjacentLeafCell = octree.adjacentCells(nodeToRegenerate, OctreeStateLeaf.getInstance());
//		for (Octree adjacentCell: adjacentLeafCell){	
//			adjacentCell.setState(OctreeStateNotYetVisited.getInstance());
//			Console.getInstance().push(adjacentCell.toString());
//		}
//
//		Console.getInstance().push("ADD "+nodeToAdd.toString());
//	}
//
//	private void removeBlockAdjacentCell(DyadicBlock nodeToRemove){
//		// regenerate only adjacent ground cell
//		// add modif to to modif save octree
//		double value =  1E16*(1 + Octree.JMAX - nodeToRemove.getJ())	;
//		// it is crucial to make the value vary with J so that recursive 
//		// modif handle the content properly
//		modif.addModif(nodeToRemove, value, 0);
//		modif.computeBounds();
//		// leafy every ground adjacent cell
//		// assume the following :
//		// time 0:
//		// a big block bb is splited. one of his son sb is set to notYetVisited.
//		// the user destroy the block adjacent sa to it 
//		// time 1:
//		// sb isvisited. the modif tree does not yet contains so block destruction of sa
//		// so that an octree event of kind killGround is thrown. (but sb is still not yet visited)
//		// the eventMediator handle the request of the user and launch retrieve the list 
//		// of adjacent ground cell (and sb is not in it)... and regenerate them
//		// time 2:
//		// sb is definitely set to ground .... not good !
//		// CONCLUSION :
//		// we need to regenerate every ground AND not yet visited adjacent cells.
//		ArrayList<Octree>adjacentGroundCell = octree.adjacentCells(nodeToRemove, OctreeStateGround.getInstance());
//		for (Octree adjacentCell: adjacentGroundCell){
//			OctreeEvent event = new OctreeEvent(adjacentCell, OctreeEventKindof.LEAFY);
//			OctreeEventMediator.addEvent(event);
//		}
//		ArrayList<Octree>adjacentNYVCell = octree.adjacentCells(nodeToRemove, OctreeStateNotYetVisited.getInstance());
//		for (Octree adjacentCell: adjacentNYVCell){
//			OctreeEvent event = new OctreeEvent(adjacentCell, OctreeEventKindof.LEAFY);
//			OctreeEventMediator.addEvent(event);
//		}
//		
//		// remove cell
//		Octree octreeToRemove = octree.smallestCellContaining(nodeToRemove);
//		OctreeEvent event = new OctreeEvent(octreeToRemove, OctreeEventKindof.KILL);
//		OctreeEventMediator.addEvent(event);
//
//		Console.getInstance().push("REMOVE "+nodeToRemove.toString());
//	}


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
						//removeBlock(nodeToRemove);
						//removeBlockAdjacentCell(nodeToRemove);
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
					//removeBlock(nodeToRemove);
					//removeBlockAdjacentCell(nodeToRemove);
				}
			}
			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_MODIF_INC_ADD){
				targetJ++;
				targetJ = Math.min(MAX_TARGET_J, Math.max(0, targetJ));
			}

			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_MODIF_DEC_ADD){
				targetJ--;
				targetJ = Math.min(MAX_TARGET_J, Math.max(0, targetJ));
			}
		}
	}

}
