package org.wavecraft.graphics.vbo;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Face;
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
}
