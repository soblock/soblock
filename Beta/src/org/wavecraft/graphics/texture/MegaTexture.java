package org.wavecraft.graphics.texture;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

// singleton
public class MegaTexture {
	private static int szAll,szOne;
	private static Texture texture;
	private static MegaTexture instance = null;

	private MegaTexture(){
		String filename = "data/megatexture.jpg";
		try {
			texture = TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		szAll = 512;
		szOne = 32;
	}

	public static MegaTexture getInstance(){
		if (instance == null){
			instance = new MegaTexture();
		}
		return instance;
	}

	public static void bind(){
		texture.bind();
	}

	//xmin xmax ymin ymax
	public static  float[] getTexCoordinate(int id){
		float epsilon = 1E-5f;
		float[] texCoord=new float[4];
		texCoord[0]= 0f;
		texCoord[1]= (szOne-epsilon)/(1.0f*szAll);
		texCoord[3]= (id*szOne)/(1.0f*szAll);
		texCoord[2]= ((id+1)*szOne-epsilon)/(1.0f*szAll);
		return texCoord;
	}

	public static float[] getTexCoordinate(int id, int normal){
		float epsilon = 1E-5f;
		float[] texCoord=new float[4];
		int idn =0; // id of normal (between 0 and 5)
		if (id == 8){
			idn = 2*(Math.abs(normal)-1);
			if (normal<0) {idn++;}
		}

		texCoord[0]= idn*(szOne-epsilon)/(1.0f*szAll);
		texCoord[1]= (idn+1)*(szOne-epsilon)/(1.0f*szAll);
		texCoord[3]= (id*szOne)/(1.0f*szAll);
		texCoord[2]= ((id+1)*szOne-epsilon)/(1.0f*szAll);
		return texCoord;
	}
}
