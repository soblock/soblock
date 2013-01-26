package org.wavecraft.graphics.vbo;

import java.util.ArrayList;
import java.util.HashMap;

import org.wavecraft.client.Timer;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.octree.Octree;
import org.wavecraft.geometry.octree.events.OctreeEvent;
import org.wavecraft.geometry.octree.events.OctreeEventListener;
import org.wavecraft.graphics.vbo.VBOWrapper.VboMode;

public class VBOFace implements OctreeEventListener {
	private HashMap<Face, Integer> positionInMemory;
	private HashMap<Face, Face> displayedFaces;
	private HashMap<Face, Face> hiddenFaces; // see method removeFace to see why 
	// we use HashMap here instead of just a HashSet.
	// A HashSet is implemented with an HashMap so since we are paying for it,
	// we might as well use an HashMap.
	public ArrayList<Octree> toDestroyNext;
	public ArrayList<Octree> toDestroy;
	private boolean[] isDead;
	private int position;
	private int size;
	private int stride;
	private int verticePerFace;
	private VBOWrapper[] vboArr;
	private int nVbo ;

	public VboMode mode;
	private boolean push = true;

	public VBOFace(int size, int nVbo, VboMode mode){
		this.size = size;
		this.nVbo = nVbo;
		this.mode = mode;
		switch (mode){
		case V3N3C4 :
			stride = 10 * 4; // number of float per vertices * 4 bytes per float
			break;
		case V3N3T2 : 
			stride = 8 * 4; // number of float per vertices * 4 bytes per float	
			break;
		}

		verticePerFace = 4 ; 
		toDestroyNext = new ArrayList<Octree>();
		toDestroy = new ArrayList<Octree>();
		hiddenFaces = new HashMap<Face,Face>();
		displayedFaces = new HashMap<Face,Face>();
		vboArr = new VBOWrapper[nVbo];
		isDead = new boolean[size*nVbo];
		for (int j =0 ;  j < nVbo; j++){
			float[] initArr = new float[size * stride * verticePerFace];
			vboArr[j] = new VBOWrapper(mode);
			vboArr[j].initFromFloat(initArr);
			float[] buff= {};
			for (int i = 0; i< size ; i++){
				isDead[i+size*j] = true;
				Face facetmp = new Face(i%80,-8*j, i/80 , 0,-2);
				switch (mode) {
				case V3N3C4:
					buff = FaceToArray.toArrayV3N3C4(facetmp);
					break;
				case V3N3T2 :
					buff = FaceToArray.toArrayV3N3T2(facetmp, 0);
				default:
					break;
				}
				vboArr[j].update(buff, verticePerFace*i);
			}
		}



		position = 0;
		positionInMemory = new HashMap<Face, Integer>();
	}

	private void findNextDead(){
		int savePose = position;
		position++;
		if (position>=size*nVbo) position = 0;
		while (!isDead[position] && position !=savePose){
			position++;
			if (position>=size*nVbo) position = 0;
		}
		if (position == savePose){
			System.out.println("buffer is full");
			System.out.println(Timer.getNframe());
		}
	}

	public int getPosition(){
		return position;
	}
	public void pushFace(Face face){
		// check if reverse face is in
		Face faceReversed = face.reverse();
		if (displayedFaces.containsKey(faceReversed)){
			// remove reverse to avoid doublon
			Face faceReversedOriginal = displayedFaces.get(faceReversed);
			hide(faceReversedOriginal);
			hiddenFaces.put(face, face);
		}
		else {
			// ask nicely 
			if (!displayedFaces.containsKey(face)){
				pushFaceWillYouPlease(face);
			}
		}
	}

	private void pushFaceWillYouPlease(Face face){
		findNextDead();
		float[] buff = null;//FaceToArray.toArrayV3N3C4(face);
		switch (mode){
		case V3N3C4:
			buff = FaceToArray.toArrayV3N3C4(face);
			break;
		case V3N3T2:
			DyadicBlock b = face.getFather();
			int content = 8;
			if (b instanceof Octree){
				content = ((Octree) b).getContent();
			}
			buff = FaceToArray.toArrayV3N3T2(face, content);
		}
		int j = position/size;
		int i = position%size;
		if (push){
			vboArr[j].update(buff, verticePerFace*i);
		}
		else {
			vboArr[j].updateNoPush(buff, verticePerFace*i);	
		}
		positionInMemory.put(face, new Integer(position));
		displayedFaces.put(face, face);
		isDead[position] = false;
	}

	private void removeFace(Face face){
		if (positionInMemory.containsKey(face)){
			removeFaceWillYouPlease(face);
		}
		if (hiddenFaces.containsKey(face)){
			hiddenFaces.remove(face);
			Face reversedFace = face.reverse();
			Face reversedFaceOriginal = hiddenFaces.get(reversedFace);
			hiddenFaces.remove(reversedFaceOriginal);
			pushFaceWillYouPlease(reversedFaceOriginal);
			// the reason we are pushing reversedFaceOriginal instead of 
			// juste reversedFace is that reversedFaceOriginal has been
			// genereted from the neighboor node and thus has been initialized
			// with the right material, whereas reversedFace has been generated 
			// from face and thus do not contains the right material.
		}
	}

	private void removeFaceWillYouPlease(Face face){
		int positionToRemove = positionInMemory.get(face);
		positionInMemory.remove(face);
		displayedFaces.remove(face);
		int j = positionToRemove/size;
		int i = positionToRemove%size;
		Face facetmp = new Face(i%80,-8*j, i/80 , 0,-2);
		float[] buff = null ;//FaceToArray.toArrayV3N3C4(facetmp);
		switch (mode) {
		case V3N3C4 : 
			buff = FaceToArray.toArrayV3N3C4(facetmp);
			break;
		case V3N3T2 :
			buff = FaceToArray.toArrayV3N3T2(facetmp, 0);
		}
		if (push){
			vboArr[j].update(buff, verticePerFace*i);
		}
		else {
			vboArr[j].updateNoPush(buff, verticePerFace*i);
		}
		isDead[positionToRemove] = true;
	}

	private void hide(Face face){
		removeFaceWillYouPlease(face);
		hiddenFaces.put(face,face);
	}

	public void pushNode(Octree node){
		Face[] faces = node.getFaces();
		for (int i = 0; i<6 ; i++){
			pushFace(faces[i]);
		}
	}

	public void removeNode(Octree node){
		Face[] faces = node.getFaces();
		for (int i = 0; i<6 ; i++){
			removeFace(faces[i]);
		}
	}

	public void draw(){
		//vbo.draw();
		for (int j=0 ; j<nVbo ; j++){
			vboArr[j].draw();
		}
		//vbo.drawOldSchool();
	}


	public void update(){
		for (int i=0;i<toDestroy.size();i++){
			removeNode(toDestroy.get(i));
		}
		toDestroy = toDestroyNext;
		toDestroyNext = new ArrayList<Octree>();
		if (!push){
			for (int j=0 ; j<nVbo ; j++){
				vboArr[j].pushAllModification();
			}
		}
	}

	@Override
	public void handle(OctreeEvent e) {
		if (e.getOctree().getJ()<20) {
			switch (e.getKindOf()) {
			case LEAFY:
				removeNode(e.getOctree());
				pushNode(e.getOctree());
				break;

			case SPLIT:
				Octree[] sons = e.getOctree().getSons();
				removeNode(e.getOctree());
				for (int i = 0 ;i<8; i++){
					pushNode(sons[i]);
				}
				break;

			case KILL:
				removeNode(e.getOctree());
				break;
			case KILLGROUND:
				removeNode(e.getOctree());
				break;
			default:
				break;
			}
		}

	}
}
