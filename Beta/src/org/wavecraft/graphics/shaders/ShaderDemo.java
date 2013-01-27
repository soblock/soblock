package org.wavecraft.graphics.shaders;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;


// from http://www.youtube.com/watch?v=zr7k7kaokSk
public class ShaderDemo {

	public static void main(String[] args){
		
		try {
			Display.setDisplayMode(new DisplayMode(640, 490));
			Display.setTitle("shader demo");
			Display.create();
		} catch(LWJGLException e){
			Display.destroy();
			System.exit(1);
		}
		
		// create program and shaders 
		int shaderProgram = glCreateProgram();
		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		
		// read source
		StringBuilder vertexShaderSource = new StringBuilder();
		StringBuilder fragmentShaderSource = new StringBuilder();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src/org/wavecraft/graphics/shaders/shader.vert"));
			String line;
			while ((line = reader.readLine()) != null){
				vertexShaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e){
			System.err.println("vertex shader wanst loaded properly");
			Display.destroy();
			System.exit(0);
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src/org/wavecraft/graphics/shaders/shader.frag"));
			String line;
			while ((line = reader.readLine()) != null){
				fragmentShaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e){
			System.err.println("fragment shader wanst loaded properly");
			Display.destroy();
			System.exit(0);
		}
		
		// link source to shader and compile
		glShaderSource(vertexShader, vertexShaderSource);
		glCompileShader(vertexShader);
		if (glGetShader(vertexShader, GL_COMPILE_STATUS) == GL_FALSE){
			System.err.println("vertex shader didnt compile correcetly");
		}
		glShaderSource(fragmentShader, fragmentShaderSource);
		glCompileShader(fragmentShader);
		if (glGetShader(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE){
			System.err.println("fragment shader didnt compile correcetly");
		}
		
		// link to program
		glAttachShader(shaderProgram,vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);
		
		while (!Display.isCloseRequested()){
			glUseProgram(shaderProgram);
			glBegin(GL_TRIANGLES);
			glColor3f(0, 0, 1);
			glVertex2f(-0.5f, 0.5f);
			glColor3f(0, 1, 0);
			glVertex2f(0.5f, 0.5f);
			glColor3f(1, 0, 0);
			glVertex2f(0.5f, -0.5f);
			glEnd();
			
			glUseProgram(0);
			Display.update();
			Display.sync(60);
		}
		glDeleteProgram(shaderProgram);
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		Display.destroy();
		System.exit(0);
	}
}
