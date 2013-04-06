package org.wavecraft.geometry;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoundingBoxTest {

	@Test
	public void test(){
		BoundingBox bb1 = new BoundingBox(new Coord3d(0, 0, 0), new Coord3d(2, 2, 2));
		BoundingBox bb2 = new BoundingBox(new Coord3d(0.5, 0.5, 0.5), new Coord3d(1.5, 1.5, 1.5));
		assertTrue(bb1.intersects(bb2));
	}
}
