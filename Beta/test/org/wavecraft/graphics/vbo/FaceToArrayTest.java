package org.wavecraft.graphics.vbo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.wavecraft.geometry.Face;
import org.wavecraft.ui.WindowFrame;

public class FaceToArrayTest {

	@Test
	public void test() {
		new WindowFrame("test");
		Face face = new Face(1, 2, 3, 4, 1);
		float[] lightAtVertice = new float[]{0, 0, 0.2f, 0.2f };
		LightFace lightFace = new LightFace(face, lightAtVertice);
		List<LightFace> listLightFace = new ArrayList<LightFace>();
		listLightFace.add(lightFace);
		float[] data = FaceToArray.toArrayV3N3T2C3(listLightFace);
		System.out.println(data.length);
		for (int i = 0 ;i<4*(3+3+2+3); i++){
			System.out.print(" "+data[i]);
			if (i%(3+3+2+3) ==3+3+2+3-1){
				System.out.println("");
			}
		}
		assertEquals(data.length, 4*(3+3+2+3) );
	}

}
