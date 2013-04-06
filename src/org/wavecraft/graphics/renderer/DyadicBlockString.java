package org.wavecraft.graphics.renderer;

import java.util.ArrayList;
import java.util.List;

import org.wavecraft.geometry.Coord3i;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Terran;
import org.wavecraft.graphics.texture.CharacterTexture;

public class DyadicBlockString {


	private static List<Blocktree> byteArrToOctreeArr(byte[] byteArr, int w,int h,int material,Coord3i ci){

		List<Blocktree> octreeArr = new ArrayList<Blocktree>();
		for (int y=0;y<h;y++){
			for (int x=0;x<w;x++){
				if (byteArr[x+w*y]!=0){
					Blocktree octree = new Blocktree(ci.x , ci.y -  x, ci.z -  y, 0);
					octree.setContent(Terran.MAN_BRICK);
					octreeArr.add(octree);
				}
			}
		}
		return octreeArr;
	}
	
	public static  List<Blocktree> stringToOctreeArr(String str,int material,Coord3i ci){
		byte[] byteArr = CharacterTexture.getInstance().string2byte(str);
		int w =str.length()*CharacterTexture.getInstance().getW();
		int h = CharacterTexture.getInstance().getH();
		return byteArrToOctreeArr(byteArr, w, h, material,ci);
	}
	
	
}