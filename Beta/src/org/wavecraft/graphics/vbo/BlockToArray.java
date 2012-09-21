package org.wavecraft.graphics.vbo;

import org.wavecraft.geometry.BoundingBox;
import org.wavecraft.geometry.octree.Octree;

public class BlockToArray {
	public static float[] toArray(Octree octree){
		BoundingBox box = new BoundingBox(octree);
		float xm = (float) box.getMinCoord3d().x;
		float ym = (float) box.getMinCoord3d().y;
		float zm = (float) box.getMinCoord3d().z;
		float xM = (float) box.getMaxCoord3d().x;
		float yM = (float) box.getMaxCoord3d().y;
		float zM = (float) box.getMaxCoord3d().z;

		float[] vertex_data_tmp = {
				// x  y    z    nx     ny     nz     r      g      b      a
				// top
				xM,  yM,  zM,  0.0f,  0.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
				xm , yM,  zM,  0.0f,  0.0f,  1.0f,  1.0f,  0.2f,  0.0f,  1.0f,
				xm , ym,  zM,  0.0f,  0.0f,  1.0f,  1.0f,  0.0f,  0.3f,  1.0f,
				xM , ym,  zM,  0.0f,  0.0f,  1.0f,  1.0f,  0.2f,  0.3f,  1.0f,

				// down
				xM,  yM,  zm,  0.0f,  0.0f,  -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
				xm , yM,  zm,  0.0f,  0.0f,  -1.0f,  1.0f,  0.4f,  0.0f,  1.0f,
				xm , ym,  zm,  0.0f,  0.0f,  -1.0f,  1.0f,  0.4f,  0.4f,  1.0f,
				xM , ym,  zm,  0.0f,  0.0f,  -1.0f,  1.0f,  0.0f,  0.4f,  1.0f,

				// front
				xm,  yM,  zM,  -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,  0.0f,  1.0f,
				xm , ym,  zM,  -1.0f,  0.0f,  0.0f,  0.3f,  1.0f,  0.0f,  1.0f,
				xm , ym,  zm,  -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,  0.3f,  1.0f,
				xm , yM,  zm,  -1.0f,  0.0f,  0.0f,  0.3f,  1.0f,  0.3f,  1.0f,
				
				// back
				xM,  yM,  zM,  +1.0f,  0.0f,  0.0f,  0.0f,  1.0f,  0.0f,  1.0f,
				xM , ym,  zM,  +1.0f,  0.0f,  0.0f,  0.0f,  1.0f,  0.3f,  1.0f,
				xM , ym,  zm,  +1.0f,  0.0f,  0.0f,  0.3f,  1.0f,  0.0f,  1.0f,
				xM , yM,  zm,  +1.0f,  0.0f,  0.0f,  0.3f,  1.0f,  0.3f,  1.0f,
				
				// left
				xM,  yM,  zM,  0.0f,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,  1.0f,
				xm , yM,  zM,  0.0f,  1.0f,  0.0f,  0.3f,  0.0f,  1.0f,  1.0f,
				xm , yM,  zm,  0.0f,  1.0f,  0.0f,  0.0f,  0.3f,  1.0f,  1.0f,
				xM , yM,  zm,  0.0f,  1.0f,  0.0f,  0.3f,  0.3f,  1.0f,  1.0f,
				
				// right
				xM,  ym,  zM,  0.0f,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,  1.0f,
				xm , ym,  zM,  0.0f,  1.0f,  0.0f,  0.3f,  0.0f,  1.0f,  1.0f,
				xm , ym,  zm,  0.0f,  1.0f,  0.0f,  0.0f,  0.3f,  1.0f,  1.0f,
				xM , ym,  zm,  0.0f,  1.0f,  0.0f,  0.3f,  0.3f,  1.0f,  1.0f,
		};
		return vertex_data_tmp;
	}
}
