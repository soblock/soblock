package org.wavecraft.geometry;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlaneTest {

	@Test
	public void test(){
		Coord3d p = new Coord3d(1, 1, 1);
		Coord3d n = new Coord3d(1, 0, 0);
		Plane plane = new Plane(p, n);

		Coord3d p2 = new Coord3d(2, 1 ,1);
		Coord3d p3 = new Coord3d(1, Math.random() ,Math.random());

		assertTrue(plane.orientedDistance(p) == 0);
		assertTrue(plane.orientedDistance(p2) == 1);
		assertTrue(plane.orientedDistance(p3) == 0);
	}

	@Test
	public void test2(){
		for (int i = 0; i<100; i++){
			Coord3d p = new Coord3d(Math.random(), Math.random(), Math.random());
			Coord3d n = new Coord3d(Math.random(), Math.random(), Math.random());
			n = n.normalize();
			Plane plane = new Plane(p, n);

			double s = Math.random();
			Coord3d p2 =  n.scalarMult(s).addInNewVector(p);

			assertTrue(Math.abs(plane.orientedDistance(p2)-s)<1E-10);
		}
	}
}
