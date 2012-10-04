package org.wavecraft.graphics.vbo;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class VertexArrayWrapper {
	private FloatBuffer buffer;

	public void initFromFloatArr(float [] floatArr){
		buffer = BufferUtils.createFloatBuffer(floatArr.length);
		buffer.put(floatArr);
	}
	
	public void delete(){
		
	}

	public void draw(){
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        buffer.rewind();
		GL11.glVertexPointer(3, 40, buffer);
		GL11.glDrawArrays(GL11.GL_QUADS, 0, buffer.capacity()/ 10);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}
}
