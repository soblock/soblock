package org.wavecraft.ui.menu;



import org.lwjgl.opengl.GL11;
import org.wavecraft.geometry.Coord2d;
import org.wavecraft.graphics.hud.TextRenderer;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventMouseClicked;
import org.wavecraft.ui.events.UiEventWindowResized;

public class WCButton implements UiEventListener{
	private static int w=1;
	private static int h=1;

	private String text;
	protected Coord2d positionRelative; // relative position of the left-up corner between -1 and 1 
	protected Coord2d sizeRelative;
	private WCAction action;

	public void setWCAction(WCAction action){
		this.action = action;
	}

	// graphic attribute
	private double[] colorFilled = { 0, 0.4, 0.4};
	private double[] colorFilledClick = { 0, 0.2, 0.2};
	private double[] colorLine = { 0.4, 0.4, 0.4};
	private double[] colorLineClick = { 0.2, 0.2, 0.2};
	private int lineWidth = 8;

	// text attribute
	private int[] textMargins = {4, 4};
	private TextRenderer textRenderer;

	private boolean isBeingClicked = false;
	// to be clicked, the user must press the 
	// left button when the cursor is on the button,
	// and release the button when the cursor is still on the button

	public WCButton(String text, Coord2d positionRelative, Coord2d sizeRelative, WCAction action){
		this.text = text;
		this.positionRelative = positionRelative;
		this.sizeRelative = sizeRelative;
		this.action = action;
		textRenderer = new TextRenderer();
		textRenderer.setFontSize(16);
		UiEventMediator.addListener(this);
	}

	public void onClick() {
		if (action != null){
			action.process();
		}
		System.out.format(" %s has been clicked %n",text);
	}

	public void draw(){
		drawFilledRectangle();
		drawRectangle();
		drawText();
	}

	public void deleteSafely(){
		UiEventMediator.removeListener(this);
	}

	protected void drawFilledRectangle(){
		if (isBeingClicked){
			GL11.glColor3d(colorFilledClick[0], colorFilledClick[1], colorFilledClick[2]);
		}
		else {
			GL11.glColor3d(colorFilled[0], colorFilled[1], colorFilled[2]);
		}
		GL11.glBegin(GL11.GL_QUADS);
		double xMin = positionRelative.x;
		double xMax = positionRelative.x+sizeRelative.x;
		double yMin = positionRelative.y;
		double yMax = positionRelative.y+sizeRelative.y;
		GL11.glVertex2d(xMin, yMin);
		GL11.glVertex2d(xMax, yMin);
		GL11.glVertex2d(xMax, yMax);
		GL11.glVertex2d(xMin, yMax);
		GL11.glEnd();
	}

	protected void drawRectangle(){
		GL11.glLineWidth(lineWidth);
		GL11.glBegin(GL11.GL_LINES);
		if (isBeingClicked){
			GL11.glColor3d(colorLineClick[0], colorLineClick[1], colorLineClick[2]);
		}
		else {
			GL11.glColor3d(colorLine[0], colorLine[1], colorLine[2]);
		}
		double xMin = positionRelative.x;
		double xMax = positionRelative.x+sizeRelative.x;
		double yMin = positionRelative.y;
		double yMax = positionRelative.y+sizeRelative.y;
		GL11.glVertex2d(xMin, yMin);
		GL11.glVertex2d(xMin, yMax);
		GL11.glVertex2d(xMin, yMax);
		GL11.glVertex2d(xMax, yMax);
		GL11.glVertex2d(xMax, yMax);
		GL11.glVertex2d(xMax, yMin);
		GL11.glVertex2d(xMax, yMin);
		GL11.glVertex2d(xMin, yMin);		
		GL11.glEnd();
	}

	protected void drawText(){
		int x = (int) (w*(positionRelative.x  +1)/2.0f) + textMargins[0];
		int y = (int) (h*(-positionRelative.y - sizeRelative.y + 1)/2.0f) + textMargins[1] ;
		//System.out.format("x %d y %d %n",x,y);
		textRenderer.drawString(text, w, h, x, y);
	}

	@Override
	public void handle(UiEvent e) {
		// TODO Auto-generated method stub
		if (e instanceof UiEventWindowResized){
			UiEventWindowResized eRs = (UiEventWindowResized) e;
			w = eRs.w;
			h = eRs.h;
		}

		if (e instanceof UiEventMouseClicked){
			UiEventMouseClicked eClicked = (UiEventMouseClicked) e;

			if (eClicked.buttonId == 0){

				// convert the click mouse point coordinate
				// into canvas coordinate
				double xRelative = 2*eClicked.position.x/(1.0f*w) - 1;
				double yRelative = 2*eClicked.position.y/(1.0f*h) - 1;

				// the cursor is in the clickable area
				double xMin = positionRelative.x;
				double xMax = positionRelative.x+sizeRelative.x;
				double yMin = positionRelative.y;
				double yMax = positionRelative.y+sizeRelative.y;
				boolean isInClickableArea = (xMin <= xRelative &&
						xRelative <= xMax &&
						yMin <= yRelative &&
						yRelative <= yMax);

				if (eClicked.isButtonPressed){
					if (isInClickableArea){
						isBeingClicked = true;
					}
				}
				else { // button is released
					if (isInClickableArea && isBeingClicked){
						onClick();
					}
					isBeingClicked = false;
				}

			}

		}
	}






}
