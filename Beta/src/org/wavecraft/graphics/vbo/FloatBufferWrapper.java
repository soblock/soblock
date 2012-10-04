package org.wavecraft.graphics.vbo;

import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;

//singleton
// use this class to avoid allocating buffer all the time 


public class FloatBufferWrapper {
	
	private HashMap<Integer, FloatBuffer> floatBufferMap; 
	FloatBuffer buffer32;
	private static FloatBufferWrapper instance;
	
	public static FloatBufferWrapper getInstance(){
		if (instance == null){
			instance = new FloatBufferWrapper();
		}
		return instance;
	}
	
	private FloatBufferWrapper(){
		floatBufferMap = new HashMap<Integer, FloatBuffer>();
		buffer32 =BufferUtils.createFloatBuffer(32);
	}
	
	
	
	
	public FloatBuffer getFloatBuffer(int size){
		if(size==32)	{
			return buffer32;
		}
		Integer isize = Integer.valueOf(size);
		if (!floatBufferMap.containsKey(isize)){
			FloatBuffer buffer = BufferUtils.createFloatBuffer(size);
			floatBufferMap.put(isize, buffer);
		}
		return floatBufferMap.get(isize);
	}
	
	
}
