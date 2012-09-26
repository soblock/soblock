package org.wavecraft.graphics.light;

import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.glEnable;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.wavecraft.client.Timer;
import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.graphics.renderer.octree.BlockRendererLines;
import org.wavecraft.graphics.renderer.octree.BlockRendererTexture;
import org.wavecraft.graphics.texture.MegaTexture;



public class Light {
	private Coord3d position;
	private static final double durationOfDayInSeconds = 10 ;
	
	public Light(){
		position = new Coord3d(4000, 4000, 4000);
	}
	
	public Light(Coord3d position){
		this.position = position ;
	}
	
	public void setPositionSunLight(){
		double x = 1024*Math.cos(2*Math.PI*Timer.getCurrT() /(1000.0 * durationOfDayInSeconds));
		double y = 512;
		double z = 1024*Math.sin(2*Math.PI*Timer.getCurrT() /(1000.0 * durationOfDayInSeconds));
		
		//x = GameEngine.getPlayer().getPosition().x;
		//y = GameEngine.getPlayer().getPosition().y;
		//z = GameEngine.getPlayer().getPosition().z-16;
		
		
		position = new Coord3d(x, y, z);
	}
	
	public void setSkyColor(){
		 GL11.glClearColor(135/255.01f, 206/255.0f, 250/255.0f, 0.0f);
	}
	
	public void draw(){
		//DyadicBlock lightBlock = new DyadicBlock((int) position.x,(int) position.y,(int) position.z,0);
		//BlockRendererLines.render(lightBlock);
		GL11.glPointSize(30);
		
		GL11.glColor3d(1,1,0);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex3f((float) position.x,(float) position.y,(float) position.z);
		GL11.glEnd();
	}
	
	public  void initLight(){
		setSkyColor();
		
		ByteBuffer temp = ByteBuffer.allocateDirect(16);
		
		temp.order(ByteOrder.nativeOrder());
		float multAmbient = 2.4f	;
		float multDiffuse = 3;
		float multSpecula = 3;
		float skyLigth[] = {135/255.01f, 206/255.0f, 250/255.0f}; 
		
		float lightAmbient[] = {skyLigth[0]* multAmbient, skyLigth[1]*multAmbient, skyLigth[2]*multAmbient, 1 };
		float lightDiffuse[] = {skyLigth[0]* multDiffuse, skyLigth[1]*multDiffuse, skyLigth[2]*multDiffuse, 1 };
		float lightSpecula[] = {skyLigth[0]* multSpecula, skyLigth[1]*multSpecula, skyLigth[2]*multSpecula, 1 };

		
		//float lightAmbient[] = { multAmbient, multAmbient, multAmbient, 1 };
	//	float lightDiffuse[] = { multDiffuse, multDiffuse, multDiffuse, 1 };
//		float lightSpecula[] = { multSpecula, multSpecula, multSpecula, 1 };

		glEnable(GL_LIGHTING);
		float lightPosition[] = { (float)position.x,(float)position.y,(float)position.z, 1.0f };
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, (FloatBuffer)temp.asFloatBuffer().put(lightAmbient).flip()  );
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer)temp.asFloatBuffer().put(lightDiffuse).flip());
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, (FloatBuffer)temp.asFloatBuffer().put(lightSpecula).flip());
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION,(FloatBuffer)temp.asFloatBuffer().put(lightPosition).flip());        
		GL11.glEnable(GL11.GL_LIGHT1); 
		   
	}
//	
//	public void initLight(){
//		// set up lighting
//		GL11.glEnable(GL11.GL_LIGHTING);
//		GL11.glEnable(GL11.GL_LIGHT0);
//
//		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, floatBuffer(1.0f, 1.0f, 1.0f, 1.0f));
//		GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 25.0f);
//
//		
//		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, floatBuffer((float) position.x ,(float) position.y,(float) position.z, 0.0f));
//
//		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, floatBuffer(1.0f, 1.0f, 1.0f, 1.0f));
//		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, floatBuffer(1.0f, 1.0f, 1.0f, 1.0f));
//
//		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, floatBuffer(0.2f, 0.2f, 0.2f, 1.0f));
//		
//
//		GL11.glShadeModel(GL11.GL_SMOOTH);
//
//		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
//		GL11.glEnable(GL11.GL_DEPTH_TEST);
//
//
//
//		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, floatBuffer(1.0f, 1.0f, 1.0f, 1.0f));
//		GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 25.0f);
//	}
//	
	public static void disableLights(){
		GL11.glDisable(GL11.GL_LIGHTING);
	}
	
	private static FloatBuffer floatBuffer(float a, float b, float c, float d) {
		float[] data = new float[]{a,b,c,d};
		FloatBuffer fb = BufferUtils.createFloatBuffer(data.length);
		fb.put(data);
		fb.flip();
		return fb;
	}

}
