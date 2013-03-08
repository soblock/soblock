package org.wavecraft.graphics.vbo;

import static org.junit.Assert.*;

import org.junit.Test;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.BlocktreeBuilder;
import org.wavecraft.geometry.blocktree.BlocktreeBuilderAdapter;
import org.wavecraft.geometry.blocktree.BlocktreeUpdaterSimple;
import org.wavecraft.geometry.blocktree.Blocktree.State;
import org.wavecraft.geometry.octree.builder.OctreeBuilderBuilder;




public class VBOBlockTreeGrandFatherTest {

	@Test
	public void test(){
		
		Blocktree blocktree = new Blocktree(0, 0, 0, 5);
		BlocktreeBuilder builder = new BlocktreeBuilderAdapter(OctreeBuilderBuilder.getFlatlandNoculling(12.5));
		

		blocktree.setState(State.GRAND_FATHER);
		BlocktreeUpdaterSimple blockTreeUpdaterSimple = new BlocktreeUpdaterSimple(builder);
		blockTreeUpdaterSimple.init(blocktree);
		
		VBOBlockTreeGrandFather vboBlocktree = new VBOBlockTreeGrandFather(blocktree);
		System.out.println(vboBlocktree.getDataSize());
		
		
		
		VBOBlockTreeGrandFather vboBlocktree2 = new VBOBlockTreeGrandFather(blocktree, builder);
		System.out.println(vboBlocktree2.getDataSize()); 
		
		assertTrue(vboBlocktree2.getDataSize()/(3+3+2+3) == vboBlocktree.getDataSize()/(3+3+2));
		//vboBlocktree.uploadToGrahpicCard();
		//vboBlocktree.unloadFromGraphicCard();
	}
	
}
