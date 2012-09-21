package org.wavecraft.graphics.hud;

import java.awt.Font;



import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;


@SuppressWarnings("deprecation")
public class TextRenderer {

	
	private  TrueTypeFont font;
	private Color fontColor;
	private String fontName;
	private int fontSize;

	
	
	public TextRenderer(){
		fontName = "Lucida Console";
		fontSize = 12;
		fontColor = Color.blue;
		reloadFont();
//		Font awtFont = new Font("Lucida Console", java.awt.Font.BOLD, 12);
//		font = new TrueTypeFont(awtFont, true);
	}
	
	private void reloadFont(){
		Font awtFont = new Font(fontName, java.awt.Font.BOLD, fontSize);
		font = new TrueTypeFont(awtFont, true);
	}
	
	public void setFontColor(double r, double g, double b){
		fontColor = new Color((float) r,(float) g,(float) b);
	}
	
	public void setFontName(String fontName){
		this.fontName = fontName;
		reloadFont();
	}
	
	public void setFontSize(int fontSize){
		this.fontSize = fontSize;
		reloadFont();
	}
	
	
	public void drawString(String string2draw,int w, int h,float x,float y){
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0, w, h, 0, 1, -1);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		font.drawString(x, y, string2draw, fontColor);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
	}
	
	public void drawStringWithNewLine(String string2draw,int x,int y){
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glLoadIdentity();
		String[] strarr =string2draw.split("\n");
		for (int i=0;i<strarr.length;i++){
			font.drawString(x, y+12*i, strarr[i], Color.blue);
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
}
