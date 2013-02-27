package org.wavecraft.graphics.vbo;

import java.util.List;

import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.graphics.rasterizer.BlocktreeRasterizer;
import org.wavecraft.graphics.vbo.VBOWrapper.VboMode;

/**
 * convert a blocktree into a vertex buffer object
 * @author laurentsifre
 *
 */
public class VBOBlockTreeGrandFather {

	private VBOWrapper vboWrapper;
	private float[] data;

	public VBOBlockTreeGrandFather(Blocktree blockTree){
		// put all face in hashset
		List<Face> allFacesFusionFast = BlocktreeRasterizer.allNonDoublonFaceFusionFast(blockTree);
		data = FaceToArray.toArrayV3N3T2(allFacesFusionFast);
		vboWrapper = new VBOWrapper(VboMode.V3N3T2);
	}
	
	public void uploadToGrahpicCard(){
		vboWrapper.initFromFloat(data);
	}
	
	public void unloadFromGraphicCard(){
		vboWrapper.delete();
	}
	
	public int getDataSize(){
		return data.length;
	}
	
	public void render(){
		vboWrapper.draw();
	}

	
}
