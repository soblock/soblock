package org.wavecraft.graphics.renderer;

import java.util.ArrayList;

import org.wavecraft.geometry.Coord3i;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.graphics.texture.CharacterTexture;

public class DyadicBlockString {

	private static DyadicBlockString instance;

	private static ArrayList<Octree> byteArrToOctreeArr(byte[] byteArr, int w,int h,int material,Coord3i ci){

		ArrayList<Octree> octreeArr = new ArrayList<Octree>();
		for (int y=0;y<h;y++){
			for (int x=0;x<w;x++){
				if (byteArr[x+w*y]!=0){
					Octree octree = new Octree(ci.x , ci.y -  x, ci.z -  y, 0);
					octree.setContent(material);
					octreeArr.add(octree);
				}
			}
		}
		return octreeArr;
	}
	
	public static  ArrayList<Octree> stringToOctreeArr(String str,int material,Coord3i ci){
		byte[] byteArr = CharacterTexture.getInstance().string2byte(str);
		int w =str.length()*CharacterTexture.getInstance().getW();
		int h = CharacterTexture.getInstance().getH();
		return byteArrToOctreeArr(byteArr, w, h, material,ci);
	}
	
	
}