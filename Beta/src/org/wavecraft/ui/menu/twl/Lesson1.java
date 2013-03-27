package org.wavecraft.ui.menu.twl;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import java.io.IOException;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class Lesson1 extends Widget {

    //The size of the LWJGL window
    private static final int WIDTH = 800, HEIGHT = 600;
    
    //TWL stuff
    private GUI gui;
    private LWJGLRenderer renderer;
    private ThemeManager themeManager;
    private Button button;

    public Lesson1() {
        setupLWJGL();
        setupOpenGL();
        initTWL();
        createButton();
    }

    /**
     * Creates the LWJGL window. Uses the size set by WIDTH and HEIGHT.
     */
    private void setupLWJGL() {
        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.setTitle("TWL Tutorial Lesson 1");
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the viewport and glOrtho to the size of the window, and sets
     * (0,0) to the top left corner (instead of the bottom left corner).
     * Also sets the clear color to black.
     */
    private void setupOpenGL() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        GL11.glViewport(0, 0, WIDTH, HEIGHT);
        GL11.glOrtho(0.0f, WIDTH, HEIGHT, 0.0f, -1.0f, 1.0f);

        GL11.glClearColor(0, 0, 0, 1);
    }

    /**
     * Initializes TWL.
     */
    private void initTWL() {
        try{
            renderer = new LWJGLRenderer();
        }catch(LWJGLException e){
            e.printStackTrace();
        }

        gui = new GUI(this, renderer);

        try{
            themeManager = ThemeManager.createThemeManager(getClass().getResource("lesson1.xml"), renderer);
        } catch(IOException e){
            e.printStackTrace();
        }

        gui.applyTheme(themeManager);
    }

    /**
     * Creates a button and adds it to the GUI.
     */
    private void createButton() {
        button = new Button("Epic button");
        button.setTheme("button");
        add(button);
    }

    protected void layout(){
        button.setPosition(100, 100);
        button.setSize(100, 33);
        //button.adjustSize();
    }

    /**
     * Loops until the LWJGL display is requested to close. Clear the screen,
     * updates the gui (which takes keyboard and mouse input from LWJGL) and
     * draws it with OpenGL. Then we call Display.update() to show the result.
     */
    private void gameloop() {
        while (!Display.isCloseRequested()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            gui.update();
            Display.update();
        }

    }

    public static void main(String[] args) {
        new Lesson1().gameloop(); //Runs the constructor and then gameloop()
    }
}