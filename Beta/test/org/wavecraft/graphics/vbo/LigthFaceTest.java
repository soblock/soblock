package org.wavecraft.graphics.vbo;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.BlocktreeBuilder;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderAdapter;
import org.wavecraft.geometry.blocktree.BlocktreeUpdaterSimple;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.octree.builder.OctreeBuilderBuilder;
import org.wavecraft.graphics.rasterizer.BlocktreeRasterizer;


public class LigthFaceTest {

	@Test
	public void test() {
		
		Blocktree blocktree = new Blocktree(0, 0, 0, 5);
		BlocktreeBuilder builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandNoculling(12.5));
		

		blocktree.setState(State.GRAND_FATHER);
		BlocktreeUpdaterSimple blockTreeUpdaterSimple = new BlocktreeUpdaterSimple(builder);
		blockTreeUpdaterSimple.init(blocktree);
		
		VBOBlockTreeGrandFather vboBlocktree = new VBOBlockTreeGrandFather(blocktree);

		List<Face> faces = BlocktreeRasterizer.allNonDoublonFaceFusionFast(blocktree);
		List<LightFace> lightFaces = LightFace.computeLight(faces, blocktree, builder);
		
		System.out.println(lightFaces);
	}

}
