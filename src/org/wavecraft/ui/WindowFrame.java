package org.wavecraft.ui;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import org.lwjgl.LWJGLException;


import org.lwjgl.opengl.Display;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;
import org.wavecraft.ui.events.UiEventWindowResized;




public class WindowFrame implements UiEventListener {

	private Canvas canvas;
	private Frame frame;


	public WindowFrame(String title){

		frame = new Frame(title);
		frame.setLayout(new BorderLayout());
		canvas = new Canvas();

		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e)
			{
				UiEvent event = new UiEventWindowResized(canvas.getWidth(),canvas.getHeight());
				UiEventMediator.getUiEventMediator().addEvent(event);
			}
		});

		frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e)
			{ canvas.requestFocusInWindow(); }
		});

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{ }//closeRequested = true; }
		});

		frame.add(canvas, BorderLayout.CENTER);

		try {
			Display.setParent(canvas);
			Display.setVSyncEnabled(true);
			frame.setPreferredSize(new Dimension(1280, 720));
			frame.setMinimumSize(new Dimension(320, 200));
			frame.pack();
			frame.setVisible(true);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		//UiEvent event = new UiEventWindowResized(canvas.getWidth(),canvas.getHeight());
		//UiEventMediator.addEvent(event);
	}

	public Dimension getSize(){
		return canvas.getSize();
	}

	public void close(){
		frame.dispose();
	}

	@Override
	public void handle(UiEvent event) {
		if (event instanceof UiEventKeyboardPressed){
			UiEventKeyboardPressed eventKeyboard = (UiEventKeyboardPressed) (event);
			switch (eventKeyboard.key) {

			case KEYBOARD_WINDOW_ENLARGE:
				Dimension dim = frame.getSize();
				Dimension newDim = new Dimension(dim.width+10,dim.height+10);
				this.frame.setSize(newDim);
				break;

			case KEYBOARD_WINDOW_REDUCE:
				Dimension dim2 = frame.getSize();
				Dimension newDim2 = new Dimension(dim2.width-10,dim2.height-10);
				this.frame.setSize(newDim2); 
			default:
				break;
			}
		}

	}




}
