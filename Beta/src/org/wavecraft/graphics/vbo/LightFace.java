package org.wavecraft.graphics.vbo;

import java.util.ArrayList;
import java.util.Arrays;
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

	public LightFace(Face face, Blocktree root, BlocktreeBuilder builder){
//		switch (face.getNormal()) {
//		case -1:
//			ligthAtVertice = new float[]{0.6f, 0.6f, 0.6f, 0.6f};
//			break;
//		case 1:
//			ligthAtVertice = new float[]{1, 1, 1, 1};
//			break;
//		case -2:
//			ligthAtVertice = new float[]{0.6f, 0.6f, 0.6f, 0.6f};
//			break;
//		case 2:
//			ligthAtVertice = new float[]{1, 1, 1, 1};
//			break;
//		case -3:
//			ligthAtVertice = new float[]{0.6f, 0.6f, 0.6f, 0.6f};
//			break;			
//		case 3:
//			ligthAtVertice = new float[]{1, 1, 1, 1};
//			break;
//
//		default:
//			ligthAtVertice = null;
//			break;
//		}
		
		//
		ligthAtVertice = new float[4];
		Coord3i[] vertices = face.getVerticesI();
		for (int i = 0; i<4 ; i ++){
			float light = 1;
			light *= getNormalLightForVertice(face);
			light *= getOclusionLigthForVertice(face, vertices[i], root, builder);
			 ligthAtVertice[i] = light;
		}
		
		//ligthAtVertice = new float[]{0, 0.25f, 0.5f, 0.75f};
		this.face = face;
	}
	
	public static float getOclusionLigthForVertice(Face face, Coord3i vertice, Blocktree root, BlocktreeBuilder builder){
		// look at the four block facing the vertice in direction of the normal
		// and count how many are made of air.
		DyadicBlock[] neighbors = face.inFrontOfVertice(vertice); 
		int airNeighborCount = 0;
		for (int i = 0; i<4; i++){
			DyadicBlock neighbor = neighbors[i];
			Blocktree find = root.smallestCellContaining(neighbor);
			if ((find == null || (find.getJ()!=neighbor.getJ() && find.getState()!=State.DEAD_AIR))){
				airNeighborCount = (builder.isIntersectingSurface(neighbor) || builder.isGround(neighbor)) ? airNeighborCount : airNeighborCount+1;
			} else {
				airNeighborCount = (find.getState()==State.DEAD_AIR) ? airNeighborCount+1 : airNeighborCount;
			}
		}
		return airNeighborCount*1.0f/4;
	}
	
	public static float getNormalLightForVertice(Face face){
		if (face.getNormal()<0) {
			return 0.6f;
		}
		else {
			return 1.0f;
		}
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
	
	public static List<LightFace> computeLight(List<Face> faces, Blocktree root, BlocktreeBuilder builder){
		List<LightFace> lightFaces = new ArrayList<LightFace>();
		for (Face face : faces){
			LightFace lightFace = new LightFace(face, root, builder);
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
