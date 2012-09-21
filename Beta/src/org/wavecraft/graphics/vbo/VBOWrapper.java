package org.wavecraft.graphics.vbo;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.wavecraft.graphics.texture.MegaTexture;


public class VBOWrapper {
	public enum VboMode{
		V3N3C4, // 3 float for position, 3 for normal, 4 for color (rgba)
		V3N3T2; // 3 float for position, 3 for normal, 2 for texture coordinate
	}
	
	private int vertex_buffer_id;
	private int size;
	private VboMode mode;
	private int stride;

	private float[] data;

	
	public VBOWrapper(VboMode mode){
		this.mode = mode;
		switch (mode) {
		case V3N3C4:
			 stride = 10;
			break;
		case V3N3T2:
			stride = 8;
			break;
		default:
			break;
		}
	}
	
	public void initFromFloat(float[] vertex_data_array){
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		GL15.glGenBuffers(buffer);
		vertex_buffer_id = buffer.get(0);
		size = vertex_data_array.length;
		data = vertex_data_array;
		//FloatBuffer vertex_buffer_data = BufferUtils.createFloatBuffer(size);
		FloatBuffer vertex_buffer_data = FloatBufferWrapper.getInstance().getFloatBuffer(vertex_data_array.length);
		vertex_buffer_data.put(data);
		vertex_buffer_data.rewind();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertex_buffer_id);
		//GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertex_buffer_data, GL15.GL_STATIC_DRAW);
		//GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertex_buffer_data, GL15.GL_DYNAMIC_DRAW);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertex_buffer_data, GL15.GL_STREAM_DRAW);
	}
	
	
	public void update(float[] vertex_data_array,int pos){
		//FloatBuffer vertex_buffer_data = BufferUtils.createFloatBuffer(vertex_data_array.length);
		FloatBuffer vertex_buffer_data = FloatBufferWrapper.getInstance().getFloatBuffer(vertex_data_array.length);
		vertex_buffer_data.put(vertex_data_array);
		vertex_buffer_data.rewind();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertex_buffer_id);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 4*stride*pos, vertex_buffer_data);
	}
	
	public void updateNoPush(float[] vertex_data_array, int pos){
		for (int i = 0; i<vertex_data_array.length; i++){
			data[i+4*stride*pos] = vertex_data_array[i];
		}
	}
	
	public void pushAllModification(){
		FloatBuffer vertex_buffer_data = FloatBufferWrapper.getInstance().getFloatBuffer(size);

		vertex_buffer_data.put(data);
		vertex_buffer_data.rewind();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertex_buffer_id);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertex_buffer_data);
		//GL15.glBufferData(null, GL15.GL_STREAM_DRAW);
		//GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertex_buffer_data, GL15.GL_STREAM_DRAW);
		
		//GL15.glDeleteBuffers(vertex_buffer_id);
		
		
		//vertex_buffer_data.clear();
	}
	
	public void delete(){
		GL15.glDeleteBuffers(vertex_buffer_id);
		
	}
	
	public void draw(){
		switch (mode){
		case V3N3C4 :
			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertex_buffer_id);
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 4*stride, 0);
			// 40 because 10 * 4 bytes
			GL11.glNormalPointer(GL11.GL_FLOAT, 4*stride, 12);
			GL11.glColorPointer(4, GL11.GL_FLOAT, 4*stride, 24);
			GL11.glDrawArrays(GL11.GL_QUADS, 0, size/ stride);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			break;
		case V3N3T2 :
			glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
			GL11.glColor3f(1, 1, 1);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			MegaTexture.bind();
			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertex_buffer_id);
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 4*stride, 0);
			GL11.glNormalPointer(GL11.GL_FLOAT, 4*stride, 12);
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 4*stride,24);
			GL11.glDrawArrays(GL11.GL_QUADS, 0, size/ stride);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);	
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		
	}

	public  void drawOldSchool(){
		GL11.glBegin(GL11.GL_QUADS);
		for (int i = 0 ;i< data.length / 10 ; i++){
			GL11.glVertex3f(data[10*i], data[10*i+1], data[10 *i+2 ]);
			GL11.glNormal3f(data[10*i+3], data[10*i+4], data[10*i+5]);
			GL11.glColor4f(data[10*i+6], data[10*i+7], data[10*i+8], data[10*i+9]);
		}
		GL11.glEnd();
	}
	
}
