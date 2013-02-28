package org.wavecraft.graphics.vbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.wavecraft.geometry.blocktree.Blocktree;

/**
 * singleton
 * this is a pool of vertex buffer object corresponding to blocktree
 * @author laurentsifre
 *
 */
public class VBOBlocktreePool {

	private static VBOBlocktreePool instance;



	private HashMap<Blocktree, VBOBlockTreeGrandFather> uploaded;

	private HashMap<Blocktree, VBOBlockTreeGrandFather> toUpload;

	private List<Blocktree> toUnload;

	public static VBOBlocktreePool getInstance(){
		if (instance == null){
			instance = new VBOBlocktreePool();
		}
		return instance;
	}

	private VBOBlocktreePool(){
		uploaded = new HashMap<Blocktree, VBOBlockTreeGrandFather>();
		toUpload = new HashMap<Blocktree, VBOBlockTreeGrandFather>();
		toUnload = new ArrayList<Blocktree>();
	}

	public void put(Blocktree blocktree, VBOBlockTreeGrandFather vbo){
		toUpload.put(blocktree, vbo);
	}

	public void prepareToUnload(Blocktree blocktree){
		toUnload.add(blocktree);
	}

	public void uploadAll(){
		for (Blocktree blocktree : toUpload.keySet()){
			VBOBlockTreeGrandFather vbo = toUpload.get(blocktree);
			vbo.uploadToGrahpicCard();
			uploaded.put(blocktree, vbo);
			System.out.println("upload "+blocktree);
			System.out.println("data size " + vbo.getDataSize());
		}
		toUpload.clear();
	}

	public void unloadAll(){
		for (Blocktree blocktree : toUnload){
			VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
			if (vbo != null){
				vbo.unloadFromGraphicCard();
				uploaded.remove(blocktree);
				System.out.println("unload "+blocktree);
			} else {
				int wtf = 1;
			}
		}
		toUnload.clear();
	}

	public void render(){
		for (Blocktree blocktree : uploaded.keySet()){
			VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
			vbo.render();
		}
	}

}
