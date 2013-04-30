package org.wavecraft.graphics.vbo;

import static org.lwjgl.opengl.GL11.glColor3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.wavecraft.gameobject.GameEngine;
import org.wavecraft.geometry.Coord2d;
import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Plane;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.graphics.GraphicEngine;
import org.wavecraft.graphics.renderer.PlaneRenderer;
import org.wavecraft.graphics.view.CameraFPS;
import org.wavecraft.graphics.view.ProjectionPerspective;
import org.wavecraft.ui.KeyboardBinding;
import org.wavecraft.ui.events.UiEvent;
import org.wavecraft.ui.events.UiEventKeyboardPressed;
import org.wavecraft.ui.events.UiEventListener;
import org.wavecraft.ui.events.UiEventMediator;

/**
 * singleton
 * this is a pool of vertex buffer object corresponding to blocktree
 * @author laurentsifre
 *
 */
public class VBOBlocktreePool implements UiEventListener{

	private enum State{
		DRAW_NOTHING,
		DRAW_ALL,
		DRAW_SMALL,
		DRAW_VIEWFRUSTRUMCULL
	}

	private static VBOBlocktreePool instance;

	private State state = State.DRAW_VIEWFRUSTRUMCULL;

	private HashMap<Blocktree, VBOBlockTreeGrandFather> uploaded;
	private HashMap<Blocktree, VBOBlockTreeGrandFather> toUpload;
	private List<Blocktree> toUnload;
	
	public static VBOBlocktreePool getInstance(){
		if (instance == null){
			instance = new VBOBlocktreePool();
		}
		return instance;
	}

	private VBOBlocktreePool(){
		UiEventMediator.getUiEventMediator().add(this);
		uploaded = new HashMap<Blocktree, VBOBlockTreeGrandFather>();
		toUpload = new HashMap<Blocktree, VBOBlockTreeGrandFather>();
		toUnload = new ArrayList<Blocktree>();
	}

	public void put(Blocktree blocktree, VBOBlockTreeGrandFather vbo){
		toUpload.put(blocktree, vbo);
	}

	public void prepareToUnload(Blocktree blocktree){
		toUnload.add(blocktree);
	}

	public void uploadAll(){
		for (Blocktree blocktree : toUpload.keySet()){
			VBOBlockTreeGrandFather vbo = toUpload.get(blocktree);
			vbo.uploadToGrahpicCard();
			uploaded.put(blocktree, vbo);
			//System.out.println("uPload "+blocktree);

		}
		toUpload.clear();
	}

	public void unloadAll(){
		for (Blocktree blocktree : toUnload){
			VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
			if (vbo != null){
				vbo.unloadFromGraphicCard();
				uploaded.remove(blocktree);
				//System.out.println("uNload "+blocktree);
			} 
			else {
				//System.out.println("did not retrieve" + blocktree);
			}
		}
		toUnload.clear();
	}

	/**
	 * removes all vbo from the pool and unload them from graphic card
	 */
	public void clearAll(){
		for (Blocktree blocktree : uploaded.keySet()){
			VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
			vbo.unloadFromGraphicCard();
		}
		uploaded.clear();
	}
	
	public void render(){
		switch (state) {
		case DRAW_ALL:
			for (Blocktree blocktree : uploaded.keySet()){
				VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
				vbo.render();
			}
			break;

		case DRAW_SMALL:
			for (Blocktree blocktree : uploaded.keySet()){
				if (blocktree.getJ()<=Blocktree.BLOCK_LOG_SIZE+1){
					VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
					vbo.render();
				}
			}
			break;

		case DRAW_VIEWFRUSTRUMCULL:
			//CameraFPS camera = ((CameraFPS) GraphicEngine.getViewMain().getCamera());
			if (GraphicEngine.getViewMain().getCamera() instanceof CameraFPS &&
					GraphicEngine.getViewMain().getProjection() instanceof ProjectionPerspective){
				CameraFPS camera = ((CameraFPS) GraphicEngine.getViewMain().getCamera());
				ProjectionPerspective prosPersp = ((ProjectionPerspective) GraphicEngine.getViewMain().getProjection());

				//			camera = new CameraFPS(new Coord3d(1024,1024,640), new Coord2d(0, 0));
				double fovy = prosPersp.getFovy();
				double aspect = prosPersp.getAspect();
				List<Plane> planes = camera.fourPlane(fovy, aspect);
				boolean debug = false;
				if (debug ){
					glColor3d(1, 0, 0);
					PlaneRenderer.render( planes.get(0) );
					glColor3d(0, 1, 0);
					PlaneRenderer.render( planes.get(1) );
					glColor3d(0, 0, 1);
					PlaneRenderer.render( planes.get(2) );
					glColor3d(1, 1, 1);
					PlaneRenderer.render( planes.get(3) );
				}
				for (Blocktree blocktree : uploaded.keySet()){
					VBOBlockTreeGrandFather vbo = uploaded.get(blocktree);
					boolean cull = false;
					int iPlane = 0;
					Coord3d center = blocktree.center();
					double edgeLength = blocktree.edgeLentgh();
					while (!cull && iPlane< 4){//planes.size() 
						Plane plane = planes.get(iPlane);
						cull = plane.orientedDistance(center) > edgeLength;
						iPlane++;
					}
					if (!cull){
						vbo.render();
					}
				}
				break;
			}

		default:
			break;
		}

	}

	@Override
	public void handle(UiEvent e) {
		if (e instanceof UiEventKeyboardPressed){
			if (((UiEventKeyboardPressed) e).key == KeyboardBinding.KEYBOARD_SWITCH_VBORAW){

				switch (state) {
				case DRAW_ALL:
					state = State.DRAW_SMALL;
					break;

				case DRAW_SMALL:
					state = State.DRAW_NOTHING;
					break;

				case DRAW_NOTHING:
					state = State.DRAW_VIEWFRUSTRUMCULL;
					break;

				case DRAW_VIEWFRUSTRUMCULL:
					state = State.DRAW_ALL;

				default:
					break;
				}
				System.out.println(state);
			}

		}
	}

}
