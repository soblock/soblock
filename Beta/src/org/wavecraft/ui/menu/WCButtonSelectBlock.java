package org.wavecraft.ui.menu;

import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.Coord2d;
import org.wavecraft.graphics.texture.MegaTexture;
import org.wavecraft.modif.ModifAdder;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;

public class WCButtonSelectBlock extends WCButton{

	private int contentId;
	class PressedButtonAction implements WCAction{
		int contentId = 0;
		public PressedButtonAction(int content){
			this.contentId = content; 
		}
		@Override
		public void process() {
			ModifAdder.setTargetContent(contentId);
		}
	}

	public WCButtonSelectBlock(int contentId, Coord2d positionRelative,
			Coord2d sizeRelative ) {
		super(String.format("%d",contentId), positionRelative, sizeRelative, null);
		// TODO Auto-generated constructor stub
		this.setWCAction(new PressedButtonAction(contentId));
		this.contentId = contentId;
	}

	protected void drawTexturedRectangle(){
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		MegaTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor3d(1, 1, 1);
		double xMin = positionRelative.x;
		double xMax = positionRelative.x+sizeRelative.x;
		double yMin = positionRelative.y;
		double yMax = positionRelative.y+sizeRelative.y;
		float[] texCoord = MegaTexture.getTexCoordinate(contentId,0);
		//texCoord for xmin xmax ymin ymax
		GL11.glTexCoord2f(texCoord[0], texCoord[2]);
		GL11.glVertex2d(xMin, yMin);

		GL11.glTexCoord2f(texCoord[1], texCoord[2]);
		GL11.glVertex2d(xMax, yMin);

		GL11.glTexCoord2f(texCoord[1], texCoord[3]);
		GL11.glVertex2d(xMax, yMax);

		GL11.glTexCoord2f(texCoord[0], texCoord[3]);
		GL11.glVertex2d(xMin, yMax);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public void draw(){
		drawTexturedRectangle();
		drawRectangle();
		drawText();
	}


	@Override 
	public void handle(UiEvent e) {
		super.handle(e);
		if (e instanceof UiEventKeyboardPressed){
			UiEventKeyboardPressed ekp = (UiEventKeyboardPressed)(e);
			int idPressed = -1;
			switch (ekp.key) {
			case KEYBOARD_CONTENT_0:
				idPressed  = 0;
				break;
			case KEYBOARD_CONTENT_1:
				idPressed  = 1;
				break;
			case KEYBOARD_CONTENT_2:
				idPressed  = 2;
				break;
			case KEYBOARD_CONTENT_3:
				idPressed  = 3;
				break;
			case KEYBOARD_CONTENT_4:
				idPressed  = 4;
				break;
			case KEYBOARD_CONTENT_5:
				idPressed  = 5;
				break;
			case KEYBOARD_CONTENT_6:
				idPressed  = 6;
				break;
			case KEYBOARD_CONTENT_7:
				idPressed  = 7;
				break;
			case KEYBOARD_CONTENT_8:
				idPressed  = 8;
				break;
			case KEYBOARD_CONTENT_9:
				idPressed  = 9;
				break;

			default:
				break;
			}
			if (idPressed == contentId){
				this.onClick();
			}
		}

	}
}
