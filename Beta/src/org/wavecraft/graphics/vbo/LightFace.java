package org.wavecraft.graphics.vbo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
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
		switch (face.getNormal()) {
		case -1:
			ligthAtVertice = new float[]{0.6f, 0.6f, 0.6f, 0.6f};
			break;
		case 1:
			ligthAtVertice = new float[]{1, 1, 1, 1};
			break;
		case -2:
			ligthAtVertice = new float[]{0.6f, 0.6f, 0.6f, 0.6f};
			break;
		case 2:
			ligthAtVertice = new float[]{1, 1, 1, 1};
			break;
		case -3:
			ligthAtVertice = new float[]{0.6f, 0.6f, 0.6f, 0.6f};
			break;			
		case 3:
			ligthAtVertice = new float[]{1, 1, 1, 1};
			break;

		default:
			ligthAtVertice = null;
			break;
		}
		//ligthAtVertice = new float[]{0, 0.25f, 0.5f, 0.75f};
		this.face = face;
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
