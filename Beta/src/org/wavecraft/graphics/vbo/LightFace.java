package org.wavecraft.graphics.vbo;

import org.wavecraft.geometry.Face;

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


}
