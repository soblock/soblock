package org.wavecraft.geometry.blocktree.modif;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.wavecraft.client.Timer;
import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.gameobject.Player;
import org.wavecraft.gameobjet.save.GameSaveAtom;
import org.wavecraft.gameobjet.save.GameSaveManager;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Terran;
import org.wavecraft.ui.KeyboardBinding;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMouseClicked;



// singleton 
public class ModifAdderBlocktree implements UiEventListener {

	private static ModifOctree modif ;
	private static Blocktree root;
	private static Terran targetContent = Terran.MAN_BRICK;
	private static int targetJ = 0; // the size of the modif
	private static final int MAX_TARGET_J = 10;
	private static ModifAdderBlocktree instance;
	private static boolean whenLeftClickAddBlock = true;

	private static List<Blocktree> toRecomputeVBO = null; 

	public static void setTargetContent(Terran content){
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
		UiEventMediator.getUiEventMediator().addListener(this);
		toRecomputeVBO = new ArrayList<Blocktree>();
	}


	public static DyadicBlock getNodeToRemove(Player player){
		// find nearest neighbor 
		Blocktree best = BlocktreeGrabber.nearestIntersectedLeaf(root, player.getPosition(), player.getVectorOfSight());
		if (best == null){return null;}
		// find best face
		Face face = best.nearestIntersectedFace(player.getPosition(), player.getVectorOfSight());
		DyadicBlock nodeToRemove = face.getFather();
		nodeToRemove = nodeToRemove.ancestor(targetJ);
		return nodeToRemove;
	}

	public static DyadicBlock getNodeToAdd(Player player){
		// find nearest neighbor 
		Blocktree best = BlocktreeGrabber.nearestIntersectedLeaf(root, player.getPosition(), player.getVectorOfSight());
		if (best == null){return null;}
		// find best face
		Face face = best.nearestIntersectedFace(player.getPosition(), player.getVectorOfSight());
		DyadicBlock nodeToAdd = face.getBlockInFrontOf();
		// check the the block does not intersect player's bounding box :

		nodeToAdd = nodeToAdd.ancestor(targetJ);
		if (nodeToAdd!=null){
			if (player.getTranslatedBoundingBox().intersects(nodeToAdd)){return null;}
		}
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

	private void addBlock(DyadicBlock nodeToAdd, Terran content){
		// modify the modif tree properly
		double value = -1E16*(1 + Blocktree.JMAX - nodeToAdd.getJ());
		// it is crucial to make the value vary with J so that recursive 
		// modif handle the content properly
		modif.addModif(nodeToAdd, value, content);
		long time = (long) Timer.getCurrT();
		GameSaveAtom atom = new GameSaveAtom(nodeToAdd, content, time, GameSaveAtom.Type.ADD);
		GameSaveManager.getInstance().getGameSave().addAtom(atom);

		modif.computeBounds();

		Blocktree nodeToRecomputeVBO = root.smallestPatriarchOrGrandFatherContaining(nodeToAdd);
		System.out.println(nodeToRecomputeVBO);
		toRecomputeVBO.add(nodeToRecomputeVBO);
		for (DyadicBlock neighbor : nodeToAdd.sixNeighbors()){
			if (neighbor!= null){
				Blocktree neighBorNodeToRecomputeVBO = root.smallestPatriarchOrGrandFatherContaining(neighbor);
				if (!toRecomputeVBO.contains(neighBorNodeToRecomputeVBO)){
					toRecomputeVBO.add(neighBorNodeToRecomputeVBO);
					System.out.println(neighBorNodeToRecomputeVBO);
				}
			}
		}
	}

	public void applyGameSaveAtom(GameSaveAtom atom){
		double value =  1E16*(1 + Blocktree.JMAX - atom.getBlock().getJ())	;
		switch (atom.getType()) {
		case ADD:
			modif.addModif(atom.getBlock(), -value, atom.getTerran());
			break;

		case REMOVE:
			modif.addModif(atom.getBlock(), value, atom.getTerran());
			break;

		default:
			break;
		}
		modif.computeBounds();
	}


	private void removeBlock(DyadicBlock nodeToRemove){
		double value =  1E16*(1 + Blocktree.JMAX - nodeToRemove.getJ())	;
		// it is crucial to make the value vary with J so that recursive 
		// modif handle the content properly
		modif.addModif(nodeToRemove, value, null);
		modif.computeBounds();
		long time = (long) Timer.getCurrT();
		GameSaveAtom atom = new GameSaveAtom(nodeToRemove, null, time, GameSaveAtom.Type.REMOVE);
		GameSaveManager.getInstance().getGameSave().addAtom(atom);

		Set<Blocktree> toRecomputeSet = new HashSet<Blocktree>();
		Blocktree nodeToRecomputeVBO = root.smallestPatriarchOrGrandFatherContaining(nodeToRemove);
		toRecomputeSet.add(nodeToRecomputeVBO);
		for (DyadicBlock neighbor : nodeToRemove.eighteenNeighbors()){
			Blocktree neighToRecomputeVBO = root.smallestPatriarchOrGrandFatherContaining(neighbor);
			toRecomputeSet.add(neighToRecomputeVBO);
		}
		toRecomputeVBO.addAll(toRecomputeSet);
	}

	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventMouseClicked && whenLeftClickAddBlock){
			UiEventMouseClicked emc = (UiEventMouseClicked) (e);
			if (emc.isButtonPressed){
				if (emc.buttonId == 0 ){
					DyadicBlock nodeToAdd = getNodeToAdd(GameEngine.getPlayer());
					if (nodeToAdd !=null){
						addBlock(nodeToAdd, targetContent);
					}
				}
				if (emc.buttonId == 1 ){
					DyadicBlock nodeToRemove = getNodeToRemove(GameEngine.getPlayer());
					if (nodeToRemove != null){	
						removeBlock(nodeToRemove);
					}
				}
			}
		}
		if (e instanceof UiEventKeyboardPressed){
			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_GAME_ADDBLOCK){
				DyadicBlock nodeToAdd = getNodeToAdd(GameEngine.getPlayer());
				if (nodeToAdd !=null){
					addBlock(nodeToAdd, targetContent);
				}
			}
			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_GAME_KILLBLOCK){
				DyadicBlock nodeToRemove = getNodeToRemove(GameEngine.getPlayer());
				if (nodeToRemove != null){
					System.out.println("node to remove : " + nodeToRemove.toString());
					removeBlock(nodeToRemove);
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
