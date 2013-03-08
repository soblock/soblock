package org.wavecraft.graphics.vbo;

import java.util.List;

import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.BlocktreeBuilder;
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

	public VBOBlockTreeGrandFather(Blocktree blocktree){
		// put all face in hashset
		List<Face> allFacesFusionFast = BlocktreeRasterizer.allNonDoublonFaceFusionFast(blocktree);
		data = FaceToArray.toArrayV3N3T2(allFacesFusionFast);
		vboWrapper = new VBOWrapper(VboMode.V3N3T2);
	}

	/**
	 * this constructor will compute the light for all vertex of all faces
	 * @param blocktree
	 * @param builder
	 */
	public VBOBlockTreeGrandFather(Blocktree blocktree, Blocktree root, BlocktreeBuilder builder){
		// put all face in hashset
		//System.out.println(blocktree);
		long t1 = System.currentTimeMillis();
		List<Face> allFacesFusionFast = BlocktreeRasterizer.allNonDoublonFaceFusionFast(blocktree);
		long t2 = System.currentTimeMillis();
		List<LightFace> lightFaces = LightFace.computeLight(allFacesFusionFast, root, builder);
		long t3 = System.currentTimeMillis();
		//System.out.println("face "+(t2-t1)+" light "+(t3-t2));
		data = FaceToArray.toArrayV3N3T2C3(lightFaces);
		vboWrapper = new VBOWrapper(VboMode.V3N3T2C3);
		//data = FaceToArray.toArrayV3N3T2(allFacesFusionFast);
		//vboWrapper = new VBOWrapper(VboMode.V3N3T2);
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
