package org.wavecraft.ui.menu;



import org.wavecraft.graphics.hud.TextRenderer;
import org.wavecraft.graphics.view.WindowSize;


// use this class for display information on the screen 
public class Console {


	private static Console instance ;

	private  String[] ringBuffer;
	private int ringBufferSz;
	private int ringBufferPos;

	TextRenderer textRenderer;

	private Console(){
		ringBufferSz = 64;
		ringBuffer = new String[ringBufferSz];
		textRenderer = new TextRenderer();
		textRenderer.setFontSize(12);
	
	}

	public static Console getInstance(){
		// lazy instantiation
		if (instance == null){
			instance = new Console();
		}
		return instance;
	}

	public void draw(){
		int posy = WindowSize.getInstance().getH()/2;
		for (int i = ringBufferPos; i>0; i--){
			process(ringBuffer[i],posy);
			posy -= 20;
		}
		// second half of buffer
		for (int i = ringBufferSz-1; i>ringBufferPos; i--){
			process(ringBuffer[i],posy);
			posy -= 20;
		}
	}

	
	public void drawBack(){
//		GL11.glBegin(GL11.GL_QUADS);
//	
//		GL11.glVertex2d(xMin, yMin);
//		GL11.glVertex2d(xMax, yMin);
//		GL11.glVertex2d(xMax, yMax);
//		GL11.glVertex2d(xMin, yMax);
//		GL11.glEnd();
	}
	public void process(String str, int posy){
		textRenderer.drawString(str, WindowSize.getInstance().getW(),  WindowSize.getInstance().getH(), 10, posy);

	}

	public void push(String str){
		ringBufferPos++;
		ringBufferPos = (ringBufferPos>ringBufferSz-1) ? 0 : ringBufferPos;
		ringBuffer[ringBufferPos] = str;
	}
}
