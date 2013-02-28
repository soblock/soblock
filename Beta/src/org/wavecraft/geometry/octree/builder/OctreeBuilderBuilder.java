package org.wavecraft.geometry.octree.builder;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.octree.cullingpriority.Culler;
import org.wavecraft.geometry.octree.cullingpriority.CullerNeverCull;
import org.wavecraft.geometry.octree.cullingpriority.CullerPosition;
import org.wavecraft.geometry.worldfunction.ThreeDimContentConstant;
import org.wavecraft.geometry.worldfunction.ThreeDimFunction;
import org.wavecraft.geometry.worldfunction.WorldFunction;
import org.wavecraft.geometry.worldfunction.WorldFunctionBuilder;
import org.wavecraft.geometry.worldfunction.WorldFunctionWrapper;
import org.wavecraft.modif.ModifOctree;


// this class builds an OctreeBuilder for en-user.

public class OctreeBuilderBuilder {
	
	public static OctreeBuilder getBuilderModif(WorldFunction wf, ModifOctree modif){
		Culler culler = new CullerPosition(GameEngine.getPlayer().getPosition());
		return new OctreeBuilderWorldFuntionCullerModif(wf,modif, culler);
	}
	
	public static OctreeBuilder getBuilder(WorldFunction wf){
		Culler culler = new CullerPosition(GameEngine.getPlayer().getPosition());
		return new OctreeBuilderWorldFuntionCuller(wf, culler);
	}
	
	public static OctreeBuilder getGeoCullingUniformFromThreeDimFunctionWithModif(ThreeDimFunction fun,ModifOctree modif){
		WorldFunction wfun = new WorldFunctionWrapper(new ThreeDimContentConstant(), fun);
		Culler culler = new CullerPosition(GameEngine.getPlayer().getPosition());
		//Culler culler = new CullerNeverCull();
		return new OctreeBuilderWorldFuntionCullerModif(wfun,modif, culler);
		
	}
	public static OctreeBuilder getFlatlandNoculling(double z0){
		WorldFunction fun = WorldFunctionBuilder.getWorldFunctionFlatUniform(z0);
		Culler culler = new CullerNeverCull();
		return new OctreeBuilderWorldFuntionCuller(fun, culler);
	}
	
	public static OctreeBuilder getFlatlandGeoCulling(double z0){
		WorldFunction fun = WorldFunctionBuilder.getWorldFunctionFlatUniform(z0);
		Culler culler = new CullerPosition(GameEngine.getPlayer().getPosition());
		return new OctreeBuilderWorldFuntionCuller(fun, culler);
	}
	
	public static OctreeBuilder getSphereNoculling(Coord3d center, double radius){
		WorldFunction fun = WorldFunctionBuilder.getWorldFunctionSphereUniform(center, radius);
		Culler culler = new CullerNeverCull();
		return new OctreeBuilderWorldFuntionCuller(fun, culler);
	}
	

	public static OctreeBuilder getSphereGeoCullin(Coord3d center, double radius){
		WorldFunction fun = WorldFunctionBuilder.getWorldFunctionSphereUniform(center, radius);
		Culler culler = new CullerPosition(GameEngine.getPlayer().getPosition());
		return new OctreeBuilderWorldFuntionCuller(fun, culler);
	}
	
	public static OctreeBuilder getSincNoCulling(Coord3d center, double scale, double deltaz, double Z0){
		WorldFunction fun = WorldFunctionBuilder.getWorldFunctionSincUniform(center, scale, deltaz, Z0);
		Culler culler = new CullerNeverCull();
		return new OctreeBuilderWorldFuntionCuller(fun, culler);
	}
	
	public static OctreeBuilder getSincGeoCulling(Coord3d center, double scale, double deltaz, double Z0){
		WorldFunction fun = WorldFunctionBuilder.getWorldFunctionSincUniform(center, scale, deltaz, Z0);
		Culler culler = new CullerPosition(GameEngine.getPlayer().getPosition());
		return new OctreeBuilderWorldFuntionCuller(fun, culler);
	}
	
	public static OctreeBuilder getPerlinGeoCulling(){
		WorldFunction fun = WorldFunctionBuilder.getWorldFunctionPerlinUniform();
		Culler culler = new CullerPosition(GameEngine.getPlayer().getPosition());
		return new OctreeBuilderWorldFuntionCuller(fun, culler);
	}
	
	public static OctreeBuilder getPerlinMSGeoCulling(){
		WorldFunction fun = WorldFunctionBuilder.getWorldFunctionPerlinMSUniform();
		Culler culler = new CullerPosition(GameEngine.getPlayer().getPosition());
		return new OctreeBuilderWorldFuntionCuller(fun, culler);
	}
}
