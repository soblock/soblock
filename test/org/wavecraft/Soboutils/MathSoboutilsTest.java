package org.wavecraft.Soboutils;
import static org.junit.Assert.*;

import org.junit.Test;


public class MathSoboutilsTest {

	@Test
	public void testDivide() {
		assertTrue(MathSoboutils.divideFloor(-1, 2)==-1);
		assertTrue(MathSoboutils.divideFloor(-10, 3)==-4);
	}

}
