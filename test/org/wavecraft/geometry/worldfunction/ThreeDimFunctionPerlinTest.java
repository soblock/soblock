package org.wavecraft.geometry.worldfunction;

import org.junit.Test;

public class ThreeDimFunctionPerlinTest {

	@Test
	public void test(){
		
		for (int i = -20; i<20; i++){
			System.out.println("i " + i + " per(i) "+ThreeDimFunctionPerlin.periodize(i, 13));
			
		}
	}
}
