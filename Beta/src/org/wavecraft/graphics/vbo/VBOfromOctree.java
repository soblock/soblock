package org.wavecraft.graphics.vbo;

import java.util.ArrayList;

import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.OctreeUtils;
import org.wavecraft.graphics.vbo.VBOWrapper.VboMode;

public class VBOfromOctree {
	public static float[] toArray(ArrayList<Octree> list){
		int K  = 4 * 6  * 10;
		float[] buffer = new float [ K * list.size()];
		for (int i = 0 ; i < list.size(); i++){
			float[] buffertmp = BlockToArray.toArray(list.get(i));
			for (int k = 0 ; k < K ; k++){
				buffer[i * K + k] = buffertmp[k];
			}
		}
		return buffer;
	}
	
	public static VBOWrapper toVBO(Octree octree){
		ArrayList<Octree> list = OctreeUtils.bufferize(octree);
		float[] buffer = toArray(list);
		VBOWrapper vbo = new VBOWrapper(VboMode.V3N3C4);
		vbo.initFromFloat(buffer);
		return vbo;
	}
}
