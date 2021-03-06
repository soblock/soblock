package org.wavecraft.graphics.texture;

import java.io.IOException;
import java.util.ArrayList;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
import org.wavecraft.geometry.blocktree.Terran;

// singleton
public class MegaTexture {
	private static int szAll,szOne;
	private static Texture texture;
	public ArrayList<int[]> terrainCoord;

	private static MegaTexture instance = null;

	private MegaTexture(){
		szAll = 256;
		szOne = 16;
	}

	/**
	 * do not load in the constructor to be able to run junit test without display
	 */
	public void loadTexture(){
		String filename = "terrain4.png";
		try {
			texture = TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int[] getMetaTexCoord(int id,int normal){
		int[] terrainCoord;
		switch (id) {
		case 0:
			terrainCoord =new int[]{0,0};
			break;

		case 2:
			if (normal==3){
				terrainCoord =new int[]{5,8};
			}
			else{
				if (normal==-3){
					terrainCoord =new int[]{2,0};
				}
				else
					terrainCoord =new int[]{3,0};
			}
			break;

		case 1 :
			terrainCoord =new int[]{0,13};
			break;
		case 3 :
			terrainCoord =new int[]{4,2};
			break;
		case 4 :
			if (normal==3){
				terrainCoord =new int[]{2,4};
			}
			else{
				if (normal==-3){
					terrainCoord =new int[]{2,0};
				}
				else
					terrainCoord =new int[]{4,4};
			}

			break;
		case 5 :
			terrainCoord =new int[]{6,0};
			break;

		case 6:
			terrainCoord =new int[]{2,0};
			break;
		case 7:
			terrainCoord =new int[]{3,1};
			break;
		case 8:
			terrainCoord =new int[]{7,0};
			break;
		case 9:
			terrainCoord =new int[]{8,0};
			break;

		case 10:
			terrainCoord =new int[]{3,4};
			break;

		case 11:
			if (normal==3){
				terrainCoord =new int[]{3,4};
			}
			else{
				if (normal==-3){
					terrainCoord =new int[]{2,0};
				}
				else
					terrainCoord =new int[]{4,4};
			}
			break;


		default:
			terrainCoord =new int[]{1,1};
			break;
		}
		return terrainCoord;
	}

	public static int[] getMetaTexCoord(Terran terran,int normal){
		int[] terrainCoord = new int[]{0,0};
		switch (terran) {
		case NAT_DIRT:
			terrainCoord =new int[]{2,0};
			break;

		case NAT_GALET:
			terrainCoord =new int[]{0,1};
			break;

			
		case NAT_GRASS:
			if (normal==3){
				terrainCoord =new int[]{5,8};
			}
			else{
				if (normal==-3){
					terrainCoord =new int[]{2,0};
				}
				else
					terrainCoord =new int[]{3,0};
			}
			break;

		case NAT_GROUNDICE:
			if (normal==3){
				terrainCoord =new int[]{2,4};
			}
			else{
				if (normal==-3){
					terrainCoord =new int[]{2,0};
				}
				else
					terrainCoord =new int[]{4,4};
			}
			break;

		case NAT_GROUNDSNOW:
			if (normal==3){
				terrainCoord =new int[]{3,4};
			}
			else{
				if (normal==-3){
					terrainCoord =new int[]{2,0};
				}
				else
					terrainCoord =new int[]{4,4};
			}
			break;
			
		case NAT_ICE:
			terrainCoord =new int[]{2,4};
			break;

		case NAT_SAND:
			terrainCoord =new int[]{2,1};
			break;

		case NAT_SOLIDSAND:
			terrainCoord =new int[]{0,13};
			break;
			
		case NAT_STONE:
			terrainCoord =new int[]{1,0};
			break;
			
		case NAT_HERBY_ROCK:
			terrainCoord =new int[]{4,2};
			break;
		case MAN_BRICK:
			terrainCoord =new int[]{7,0};
			break;
			
		case MAN_PARQUET:
			terrainCoord =new int[]{4,0};
			break;
			
		case  MAN_METAL:
			terrainCoord =new int[]{6,0};
			break;
		default:
			break;

		}
		return terrainCoord;
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
	public  float[] getTexCoordinate(int id){
		float epsilon = 1E-5f;
		float[] texCoord=new float[4];
		texCoord[0]= 0f;
		texCoord[1]= (szOne-epsilon)/(1.0f*szAll);
		texCoord[3]= (id*szOne)/(1.0f*szAll);
		texCoord[2]= ((id+1)*szOne-epsilon)/(1.0f*szAll);
		return texCoord;
	}

	public float[] getTexCoordinate(int id, int normal){
		//float epsilon = 1E-5f;
		float epsilon = 1.01f;
		float[] texCoord=new float[4];
		int idn =0; // id of normal (between 0 and 5)
		if (id == 8){
			idn = 2*(Math.abs(normal)-1);
			if (normal<0) {idn++;}
		}

		int[] metaTex = getMetaTexCoord(id, normal);

		idn = metaTex[0];
		id = metaTex[1];
		//System.out.println(idn);
		//System.out.println(id);
		//		texCoord[0]= idn*(szOne-epsilon)/(1.0f*szAll);
		//		texCoord[1]= (idn+1)*(szOne-epsilon)/(1.0f*szAll);
		//		texCoord[3]= (id*szOne)/(1.0f*szAll);
		//		texCoord[2]= ((id+1)*szOne-epsilon)/(1.0f*szAll);
		texCoord[0]= (idn*szOne+epsilon)/(1.0f*szAll);
		texCoord[1]= ((idn+1)*szOne-epsilon)/(1.0f*szAll);
		texCoord[3]= (id*szOne+epsilon)/(1.0f*szAll);
		texCoord[2]= ((id+1)*szOne-epsilon)/(1.0f*szAll);

		return texCoord;
	}

	public float[] getTexCoordinate(Terran terran, int normal){
		//float epsilon = 1E-5f;
		float epsilon = 1.01f;
		float[] texCoord=new float[4];
		

		int[] metaTex = getMetaTexCoord(terran, normal);

		int idn = metaTex[0];
		int id = metaTex[1];
		//System.out.println(idn);
		//System.out.println(id);
		//				texCoord[0]= idn*(szOne-epsilon)/(1.0f*szAll);
		//				texCoord[1]= (idn+1)*(szOne-epsilon)/(1.0f*szAll);
		//				texCoord[3]= (id*szOne)/(1.0f*szAll);
		//				texCoord[2]= ((id+1)*szOne-epsilon)/(1.0f*szAll);
		texCoord[0]= (idn*szOne+epsilon)/(1.0f*szAll);
		texCoord[1]= ((idn+1)*szOne-epsilon)/(1.0f*szAll);
		texCoord[3]= (id*szOne+epsilon)/(1.0f*szAll);
		texCoord[2]= ((id+1)*szOne-epsilon)/(1.0f*szAll);

		return texCoord;
	}

}
