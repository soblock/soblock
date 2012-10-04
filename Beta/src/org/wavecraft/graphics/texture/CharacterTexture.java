package org.wavecraft.graphics.texture;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class CharacterTexture {

	private static Texture character;

	private static CharacterTexture instance;
	public String[] charPos;
	public HashMap<String, Integer> posmap;
	private int h=16;
	private int w=6;
	private int offsetx=2;
	private byte[] value;
	private ArrayList<byte[]> img_db;

	private CharacterTexture(){
		String filenamechar = "data/char.png";
		try {
			character = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(filenamechar));
		} catch (IOException e) {
			e.printStackTrace();
		}
		charPos =new String[] {"q","w","e","r","t","y","u","i","o","p","a","s","d","f","g","h","j","k","l",
				"z","x","c","v","b","n","m"};
		posmap = new HashMap<String, Integer>();
		img_db = new ArrayList<byte[]>();
		value = character.getTextureData();
		for (int i =0 ;i<charPos.length;i++){
			posmap.put(charPos[i],new Integer(i));
			img_db.add(getBooleanMatrix(i));
		}
	}


	public int getW(){
		return w;
	}
	
	public int getH(){
		return h;
	}
	public int getCharPos(String str){
		if (posmap.containsKey(str)){
			return posmap.get(str).intValue();
		}
		return -1;
	}

	public void printChar(int i){
		
		printByteArr(img_db.get(i), h, w);
	}
	
	public void printByteArr(byte[] byteArr,int h1, int w1){
		for (int y = 0;y<h1;y++){
			System.out.println();
			for (int x = 0;x<w1;x++){
				System.out.print(byteArr[x+w1*y]);
			}
		}	
	}

	public byte[] string2byte(String str){
		byte[] byteArr= new byte[h*w*str.length()];
		int w1 =  w*str.length();
		for (int i=0;i<str.length();i++){

			int pos = getCharPos(str.substring(i, i+1));
			
			if (pos>=0){
				for (int y = 0;y<h;y++){
					for (int x = 0;x<w;x++){
						byteArr[ x + w*i + w1*y]= img_db.get(pos)[x+w*y];
					}
				}
			}
		}
		return byteArr;
	}
	
	public void printString(String str){
		byte[] byteArr = string2byte(str);
		printByteArr(byteArr, h, w*str.length());
	}
	

	public byte[] getBooleanMatrix(int i){
		byte[] img=new byte[h*w];

		for (int y = 0;y<h;y++)
			for (int x = 0;x<w;x++){
				byte bb = value[3*(x+ 512*y)];
				img[x+w*y] = (byte) (value[3*(x + offsetx + i*w + 512*y)] + 1);
			}
		return img;
	}

	public static CharacterTexture getInstance(){
		if (instance == null){
			instance = new CharacterTexture();
		}
		return instance;
	}


}
