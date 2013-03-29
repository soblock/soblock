package org.wavecraft.graphics.vbo;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.BlocktreeBuilder;

import org.wavecraft.geometry.blocktree.BlocktreeUpdaterSimple;
import org.wavecraft.geometry.blocktree.Blocktree.State;

import org.wavecraft.graphics.rasterizer.BlocktreeRasterizer;


public class LigthFaceTest {

	@Test
	public void test() {
		
		Blocktree blocktree = new Blocktree(0, 0, 0, 5);
		BlocktreeBuilder builder = null;// TODO : fix
		

		blocktree.setState(State.GRAND_FATHER);
		BlocktreeUpdaterSimple blockTreeUpdaterSimple = new BlocktreeUpdaterSimple(builder);
		blockTreeUpdaterSimple.init(blocktree);
		
		VBOBlockTreeGrandFather vboBlocktree = new VBOBlockTreeGrandFather(blocktree);

		List<Face> faces = BlocktreeRasterizer.allNonDoublonFaceFusionFast(blocktree);
		List<Blocktree> blocktrees = blocktree.listOfGreatChildren();
		HashMap<DyadicBlock, Float> cache = LightFace.initCacheFromBlocktree(blocktrees);
		List<LightFace> lightFaces = LightFace.computeLightCache(faces, cache, builder);
		System.out.println(lightFaces.size());
	}

}
