package org.wavecraft.graphics.vbo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Coord3i;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.blocktree.BlocktreeBuilder;

public class LightFace {


	private final float[] ligthAtVertice;
	private final Face face;

	/**
	 * @param face
	 * @param lightAtVertice a float array of size 4 containing the light at each vertex between 0 and 1 (0 = dark, 1 = light)
	 */
	public LightFace(Face face,float[] lightAtVertice){
		this.face = face;
		this.ligthAtVertice = lightAtVertice;
	}

	
	public LightFace(Face face, HashMap<DyadicBlock, Float> cache, BlocktreeBuilder builder){
		ligthAtVertice = new float[4];		
		Coord3i[] vertices = face.getVerticesI();
		float lightFace = getNormalLightForVertice(face)*getSizeFaceLight(face);
		for (int i = 0; i<4 ; i ++){
			float light = lightFace;
			light *= getOclusionlightForVerticeCache(face, vertices[i], cache, builder);
			ligthAtVertice[i] = light;
		}
		this.face = face;
		
	}
	
	
	public static float getOclusionlightForVerticeCache(Face face, Coord3i vertice, HashMap<DyadicBlock, Float> cache, BlocktreeBuilder builder){
		DyadicBlock[] neighbors = face.inFrontOfVertice(vertice);
		int airNeighborCount = 0;
		for (int i = 0; i<4; i++){
			DyadicBlock neighbor = neighbors[i];
			// check if in the cache
			if (cache.containsKey(neighbor)){
				Float value = cache.get(neighbor);
				airNeighborCount += value;
			}
			else {
				float value = (builder.isIntersectingSurface(neighbor) || builder.isGround(neighbor)) ? 0 : 1;
				airNeighborCount += value;
				cache.put(neighbor, new Float(value));
			}
		}
		return airNeighborCount*1.0f/8 + 1.0f/2;
	}
	
	public static float getNormalLightForVertice(Face face){
		if (face.getNormal()<0) {
			return 0.6f;
		}
		else {
			return 1.0f;
		}
	}
	
	public static float getSizeFaceLight(Face face){
		return 1-face.getJ()*1.0f/5;
	}
	
	public static HashMap<DyadicBlock , Float> initCacheFromBlocktree(List<Blocktree> blocktrees){
		HashMap<DyadicBlock, Float> cache = new HashMap<DyadicBlock, Float>();
		for (Blocktree blocktree : blocktrees){
			switch (blocktree.getState()) {
			case LEAF: case DEAD_GROUND: 
				cache.put(blocktree, new Float(0));
				break;

			case DEAD_AIR :
				cache.put(blocktree, new Float(1));
				break;
			default:
				throw new IllegalArgumentException("init cache only from grand father");
			}
		}
		return cache;
	}
	
	/**
	 * 
	 * @return a float array of size 4 containing the light at each vertex between 0 and 1 (0 = dark, 1 = light)
	 */
	public float[] getLigthAtVertice() {
		return ligthAtVertice;
	}

	public Face getFace() {
		return face;
	}
	
	public static List<LightFace> computeLightCache(List<Face> faces, HashMap<DyadicBlock, Float> cache, BlocktreeBuilder builder){
		List<LightFace> lightFaces = new ArrayList<LightFace>();
		for (Face face : faces){
			LightFace lightFace = new LightFace(face, cache, builder);
			lightFaces.add(lightFace);
		}
		return lightFaces;
	}
	
	@Override
	public String toString() {
		return "LightFace [ligthAtVertice=" + Arrays.toString(ligthAtVertice)
				+ ", face=" + face + "] ";
	}



}
