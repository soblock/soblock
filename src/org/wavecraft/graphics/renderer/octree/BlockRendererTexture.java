package org.wavecraft.graphics.renderer.octree;



import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.wavecraft.client.Timer;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Terran;

import org.wavecraft.graphics.texture.MegaTexture;
import org.wavecraft.graphics.vbo.FaceToArray;

public class BlockRendererTexture {

	
	public static void afterGLQuads2(DyadicBlock block){
		int id = 0;
		
		//id = 9;
		// get all faces
		Face[] faces = block.getFaces();
		
		for (int i = 0 ; i<6 ; i ++){
			float[] faceData;
				faceData = FaceToArray.toArrayV3N3T2(faces[i], Terran.MAN_BRICK);
		
			for (int vertexid = 0 ; vertexid<4;vertexid++){
				
				GL11.glTexCoord2d(faceData[8*vertexid + 6],faceData[8*vertexid + 7]);
				GL11.glNormal3f(faceData[8*vertexid + 3],faceData[8*vertexid + 4],faceData[8*vertexid + 5]);
				GL11.glVertex3f(faceData[8*vertexid + 0],faceData[8*vertexid + 1],faceData[8*vertexid + 2]);
			}
		}
	}
	
	// DEPRECATED
	// extremely inefficent, for debug only
	public static void afterGLQuads(DyadicBlock block){
		int id = (Timer.getNframe()/20)%7;
		
		float[] texCoord = MegaTexture.getInstance().getTexCoordinate(id);
		GL11.glTexCoord2d(0, 0);
		
		float ttj  = (float) Math.pow(2, block.getJ());
		float x = block.x*ttj;
		float y = block.y*ttj;
		float z = block.z*ttj;
		for (int i = 0 ; i< 4 ; i++){
			//System.out.println(texCoord[i]);
		}
		
		GL11.glTexCoord2d(texCoord[0], texCoord[2]);
		GL11.glVertex3f(x, y,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[2]);
		GL11.glVertex3f(x, y+ttj,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[3]);
		GL11.glVertex3f(x+ttj, y+ttj,z);
		GL11.glTexCoord2d(texCoord[0], texCoord[3]);
		GL11.glVertex3f(x+ttj, y,z);

		GL11.glTexCoord2d(texCoord[0], texCoord[2]);
		GL11.glVertex3f(x, y,z+ttj);
		GL11.glTexCoord2d(texCoord[1], texCoord[2]);
		GL11.glVertex3f(x, y+ttj,z+ttj);
		GL11.glTexCoord2d(texCoord[1], texCoord[3]);
		GL11.glVertex3f(x+ttj, y+ttj,z+ttj);
		GL11.glTexCoord2d(texCoord[0], texCoord[3]);
		GL11.glVertex3f(x+ttj, y,z+ttj);

		GL11.glTexCoord2d(texCoord[0], texCoord[2]);
		GL11.glVertex3f(x, y,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[2]);
		GL11.glVertex3f(x, y+ttj,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[3]);
		GL11.glVertex3f(x, y+ttj,z+ttj);
		GL11.glTexCoord2d(texCoord[0], texCoord[3]);
		GL11.glVertex3f(x, y,z+ttj);

		GL11.glTexCoord2d(texCoord[0], texCoord[2]);
		GL11.glVertex3f(x+ttj, y,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[2]);
		GL11.glVertex3f(x+ttj, y+ttj,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[3]);
		GL11.glVertex3f(x+ttj, y+ttj,z+ttj);
		GL11.glTexCoord2d(texCoord[0], texCoord[3]);
		GL11.glVertex3f(x+ttj, y,z+ttj);

		GL11.glTexCoord2d(texCoord[0], texCoord[2]);
		GL11.glVertex3f(x, y,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[2]);
		GL11.glVertex3f(x+ttj, y,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[3]);
		GL11.glVertex3f(x+ttj, y,z+ttj);
		GL11.glTexCoord2d(texCoord[0], texCoord[3]);
		GL11.glVertex3f(x, y,z+ttj);

		GL11.glTexCoord2d(texCoord[0], texCoord[2]);
		GL11.glVertex3f(x, y+ttj,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[2]);
		GL11.glVertex3f(x+ttj, y+ttj,z);
		GL11.glTexCoord2d(texCoord[1], texCoord[3]);
		GL11.glVertex3f(x+ttj, y+ttj,z+ttj);
		GL11.glTexCoord2d(texCoord[0], texCoord[3]);
		GL11.glVertex3f(x, y+ttj,z+ttj);
	}
	
	public static void render(DyadicBlock block){
		MegaTexture.getInstance();
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		MegaTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor3d(1, 1, 1);
		afterGLQuads(block);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	
	public static void render(ArrayList<DyadicBlock> blockArr){
		MegaTexture.getInstance();
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		MegaTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0 ; i< blockArr.size(); i++){
			afterGLQuads2(blockArr.get(i));
		}
		GL11.glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
}
