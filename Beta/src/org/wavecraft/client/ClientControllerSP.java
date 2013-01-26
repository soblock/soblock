package org.wavecraft.client;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.gameobject.Player;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.builder.OctreeBuilder;
import org.wavecraft.geometry.octree.builder.OctreeBuilderWorldFuntionCullerModif;
import org.wavecraft.graphics.GraphicEngine;
import org.wavecraft.graphics.hud.HUD;
import org.wavecraft.graphics.hud.HUDBuilder;
import org.wavecraft.graphics.view.ViewBuilder;
import org.wavecraft.modif.ModifAdder;
import org.wavecraft.modif.ModifOctree;


// this class is the Client Controller
// it handles binding between graphics and gamengine objects
// singleton
public class ClientControllerSP {
	private static ClientControllerSP clientControllerSP = null;
	
	private ClientControllerSP(){
		
	}
	
	public static ClientControllerSP getClientControllerSP(){
		if (clientControllerSP == null){
			clientControllerSP = new ClientControllerSP();
		}
		return clientControllerSP;
	}
	

	// get the views of graphics point in the direction the player is looking at
	private static void bindView(){
		Player player = GameEngine.getPlayer();
		ViewBuilder.bind(player,  GraphicEngine.getViewMain());
		ViewBuilder.bind(player,  GraphicEngine.getViewMinimap());
	}
	
	
	private static void bindHud(){
		HUD hud = GraphicEngine.getHud();
		HUDBuilder.bind(hud);
	}
	
	private static void bindModifAdder(){
		Octree octree = GameEngine.getOctree();
		ModifAdder.setOctree(octree);
		// check if OctreeBuilder contains a modif
		// if so, init the Modif adder with the modif field.
		OctreeBuilder builder = GameEngine.getOctreeBuilder();
		if (builder instanceof OctreeBuilderWorldFuntionCullerModif){
			ModifOctree modif = ((OctreeBuilderWorldFuntionCullerModif) builder).getModifOctree();
			ModifAdder.setModif(modif);
		}
		
	}
	
	public static void bindStuff(){
		bindView();
		bindHud();
		bindModifAdder();
	}
}
