package org.wavecraft.graphics.vbo;

import java.util.List;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Face;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.graphics.texture.MegaTexture;

public class FaceToArray {
	public static float[] toArrayV3N3C4(Face face){
		Coord3d[] vertices = face.vertices();
		float[] rgb = new float[3];
		float colorCoefft = 1.0f - face.getJ()/10.0f; 
		switch (face.getNormal()) {
		case 1 : case -1 :
			rgb[0]= 0.8f * colorCoefft;
			break ; 
		case 2 : case -2 :
			rgb[1]= 0.8f * colorCoefft;
			break ; 
		case 3 : case -3 :
			rgb[2]= 0.8f * colorCoefft;
			break;

		default:
			break;
		}

		float[] nxyz = new float[3];
		switch (face.getNormal()) {
		case 1:
			nxyz[0] = -1;
		case -1:
			nxyz[0] = 1;
		case 2:
			nxyz[1] = -1;
		case -2:
			nxyz[1] = 1;
		case 3:
			nxyz[2] = -1;
		case -3:
			nxyz[2] = 1;
		}
		float[] vertex_data_tmp = {
				// x  y    z    nx     ny     nz     r      g      b      a
				(float) vertices[0].x, (float) vertices[0].y,  (float) vertices[0].z,  nxyz[0],  nxyz[1],  nxyz[2],  rgb[0], rgb[1],  rgb[2]+0.0f,  1.0f,
				(float) vertices[1].x, (float) vertices[1].y,  (float) vertices[1].z,  nxyz[0],  nxyz[1],  nxyz[2],  rgb[0],  rgb[1] + 0.2f, rgb[2]+ 0.0f,  1.0f,
				(float) vertices[2].x, (float) vertices[2].y,  (float) vertices[2].z,  nxyz[0],  nxyz[1],  nxyz[2],  rgb[0],  rgb[1] + 0.0f, rgb[2]+ 0.3f,  1.0f,
				(float) vertices[3].x ,(float) vertices[3].y,  (float) vertices[3].z,  nxyz[0],  nxyz[1],  nxyz[2],  rgb[0],  rgb[1] + 0.2f, rgb[2]+ 0.3f,  1.0f,
		};
		return vertex_data_tmp;
	}


	public static float[] toArrayV3N3T2(Face face, int id){
		float[] textCoord = MegaTexture.getTexCoordinate(id,face.getNormal());

		float txmin = textCoord[0];
		float txmax = textCoord[1];
		float tymin = textCoord[2];
		float tymax = textCoord[3];
		float ttj = (float) Math.pow(2, face.getJ());
		float xmin = (float) (ttj*face.coord.x);
		float ymin = (float) (ttj*face.coord.y);
		float zmin = (float) (ttj*face.coord.z);

		switch (face.getNormal()){
		case 1 :{
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,     +1,      0,       0,     txmin,    tymin,
					xmin,        ymin+ttj,  zmin,     +1,      0,       0,     txmax,    tymin,
					xmin,        ymin+ttj,  zmin+ttj, +1,      0,       0,     txmax,    tymax,
					xmin,        ymin,      zmin+ttj, +1,      0,       0,     txmin,    tymax
			};
			return data;
		}
		case -1 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,     -1,      0,       0,     txmax,    tymin,
					xmin,        ymin,      zmin+ttj, -1,      0,       0,     txmax,    tymax,
					xmin,        ymin+ttj,  zmin+ttj, -1,      0,       0,     txmin,    tymax,
					xmin,        ymin+ttj,  zmin,     -1,      0,       0,     txmin,    tymin
			};
			return data;
		}
		case 2 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,     +1,       0,     txmax,    tymin,
					xmin,        ymin,      zmin+ttj,  0,     +1,       0,     txmax,    tymax,
					xmin+ttj,    ymin,  	zmin+ttj,  0,     +1,       0,     txmin,    tymax,
					xmin+ttj,    ymin,      zmin,      0,     +1,       0,     txmin,    tymin
			};
			return data;
		}
		case -2 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,     -1,       0,     txmin,    tymin,
					xmin+ttj,    ymin,      zmin,      0,     -1,       0,     txmax,    tymin,
					xmin+ttj,    ymin,  	zmin+ttj,  0,     -1,       0,     txmax,    tymax,
					xmin,        ymin,      zmin+ttj,  0,     -1,       0,     txmin,    tymax
			};
			return data;
		}
		case -3 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,      0,      -1,     txmax,    tymin,
					xmin    ,    ymin+ttj,  zmin,      0,      0,      -1,     txmin,    tymin,
					xmin+ttj,    ymin+ttj,  zmin,      0,      0,      -1,     txmin,    tymax,
					xmin+ttj,    ymin,      zmin,      0,      0,      -1,     txmax,    tymax
			};
			return data;
		}
		case 3 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,      0,      +1,     txmin,    tymin,
					xmin+ttj,    ymin,      zmin,      0,      0,      +1,     txmin,    tymax,
					xmin+ttj,    ymin+ttj,  zmin,      0,      0,      +1,     txmax,    tymax,
					xmin    ,    ymin+ttj,  zmin,      0,      0,      +1,     txmax,    tymin
			};
			return data;
		}
		}
		return null;
	}


	public static float[] toArrayV3N3T2partlyFilledbis(Face face, int id,float w){
		float[] textCoord = MegaTexture.getTexCoordinate(id,face.getNormal());

		float txmin = textCoord[0];
		float txmax = textCoord[1];
		float tymin = textCoord[2];
		float tymax = textCoord[3];
		float ttj = (float) Math.pow(2, face.getJ());
		float xmin = (float) (ttj*(face.coord.x+0.5)-w/2.);
		float ymin = (float) (ttj*(face.coord.y+0.5)-w/2.);
		float zmin = (float) (ttj*face.coord.z);

		switch (face.getNormal()){
		case 1 :{
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,     +1,      0,       0,     txmin,    tymin,
					xmin,        ymin+w,    zmin,     +1,      0,       0,     txmax,    tymin,
					xmin,        ymin+w,    zmin+ttj, +1,      0,       0,     txmax,    tymax,
					xmin,        ymin,      zmin+ttj, +1,      0,       0,     txmin,    tymax
			};
			return data;
		}
		case -1 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,     -1,      0,       0,     txmax,    tymin,
					xmin,        ymin,      zmin+ttj,   -1,      0,       0,     txmax,    tymax,
					xmin,        ymin+w,    zmin+ttj,   -1,      0,       0,     txmin,    tymax,
					xmin,        ymin+w,    zmin,     -1,      0,       0,     txmin,    tymin
			};
			return data;
		}
		case 2 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,     +1,       0,     txmax,    tymin,
					xmin,        ymin,      zmin+ttj,    0,     +1,       0,     txmax,    tymax,
					xmin+w,      ymin,  	zmin+ttj,    0,     +1,       0,     txmin,    tymax,
					xmin+w,      ymin,      zmin,      0,     +1,       0,     txmin,    tymin
			};
			return data;
		}
		case -2 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,     -1,       0,     txmin,    tymin,
					xmin+w,      ymin,      zmin,      0,     -1,       0,     txmax,    tymin,
					xmin+w,      ymin,  	zmin+ttj,  0,     -1,       0,     txmax,    tymax,
					xmin,        ymin,      zmin+ttj,  0,     -1,       0,     txmin,    tymax
			};
			return data;
		}
		case -3 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,      0,      -1,     txmax,    tymin,
					xmin    ,    ymin+w,    zmin,      0,      0,      -1,     txmin,    tymin,
					xmin+w,      ymin+w,    zmin,      0,      0,      -1,     txmin,    tymax,
					xmin+w,      ymin,      zmin,      0,      0,      -1,     txmax,    tymax
			};
			return data;
		}
		case 3 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,      0,      +1,     txmin,    tymin,
					xmin+w,      ymin,      zmin,      0,      0,      +1,     txmin,    tymax,
					xmin+w,      ymin+w,    zmin,      0,      0,      +1,     txmax,    tymax,
					xmin    ,    ymin+w,    zmin,      0,      0,      +1,     txmax,    tymin
			};
			return data;
		}
		}
		return null;
	}
	public static float[] toArrayV3N3T2partlyFilled(Face face, int id,float heightOfFluid){
		float[] textCoord = MegaTexture.getTexCoordinate(id,face.getNormal());

		float txmin = textCoord[0];
		float txmax = textCoord[1];
		float tymin = textCoord[2];
		float tymax = textCoord[3];
		float ttj = (float) Math.pow(2, face.getJ());
		float xmin = (float) (ttj*face.coord.x);
		float ymin = (float) (ttj*face.coord.y);
		float zmin = (float) (ttj*face.coord.z);

		switch (face.getNormal()){
		case 1 :{
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,     +1,      0,       0,     txmin,    tymin,
					xmin,        ymin+ttj,  zmin,     +1,      0,       0,     txmax,    tymin,
					xmin,        ymin+ttj,  zmin+heightOfFluid, +1,      0,       0,     txmax,    tymax,
					xmin,        ymin,      zmin+heightOfFluid, +1,      0,       0,     txmin,    tymax
			};
			return data;
		}
		case -1 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,     -1,      0,       0,     txmax,    tymin,
					xmin,        ymin,      zmin+heightOfFluid, -1,      0,       0,     txmax,    tymax,
					xmin,        ymin+ttj,  zmin+heightOfFluid, -1,      0,       0,     txmin,    tymax,
					xmin,        ymin+ttj,  zmin,     -1,      0,       0,     txmin,    tymin
			};
			return data;
		}
		case 2 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,     +1,       0,     txmax,    tymin,
					xmin,        ymin,      zmin+heightOfFluid,  0,     +1,       0,     txmax,    tymax,
					xmin+ttj,    ymin,  	zmin+heightOfFluid,  0,     +1,       0,     txmin,    tymax,
					xmin+ttj,    ymin,      zmin,      0,     +1,       0,     txmin,    tymin
			};
			return data;
		}
		case -2 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,     -1,       0,     txmin,    tymin,
					xmin+ttj,    ymin,      zmin,      0,     -1,       0,     txmax,    tymin,
					xmin+ttj,    ymin,  	zmin+heightOfFluid,  0,     -1,       0,     txmax,    tymax,
					xmin,        ymin,      zmin+heightOfFluid,  0,     -1,       0,     txmin,    tymax
			};
			return data;
		}
		case -3 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin,      0,      0,      -1,     txmax,    tymin,
					xmin    ,    ymin+ttj,  zmin,      0,      0,      -1,     txmin,    tymin,
					xmin+ttj,    ymin+ttj,  zmin,      0,      0,      -1,     txmin,    tymax,
					xmin+ttj,    ymin,      zmin,      0,      0,      -1,     txmax,    tymax
			};
			return data;
		}
		case 3 :{ 
			float[] data = {//x, y          z        nx       ny       nz       tx        ty 
					xmin,        ymin,      zmin-(ttj-heightOfFluid),      0,      0,      +1,     txmin,    tymin,
					xmin+ttj,    ymin,      zmin-(ttj-heightOfFluid),      0,      0,      +1,     txmin,    tymax,
					xmin+ttj,    ymin+ttj,  zmin-(ttj-heightOfFluid),      0,      0,      +1,     txmax,    tymax,
					xmin    ,    ymin+ttj,  zmin-(ttj-heightOfFluid),      0,      0,      +1,     txmax,    tymin
			};
			return data;
		}
		}
		return null;
	}


	public static float[] toArrayV3N3T2(List<Face> faces){
		int dataSz = ( 3 + 3 + 2) * 4 * faces.size();
		float[] data = new float[dataSz];

		for (int i = 0; i < faces.size(); i++){
			Face face = faces.get(i);

			int offset = i*(3 + 3 + 2)*4;
			DyadicBlock block = face.getFather();
			int content = 0;
			if (block instanceof Blocktree){
				content = ((Blocktree) block).getContent();
			}
			float[] dataFace = toArrayV3N3T2(face, content);
			for (int k = 0; k < dataFace.length; k++){
				data[offset + k] = dataFace[k]; 
			}
		}
		return data;

	}
}
