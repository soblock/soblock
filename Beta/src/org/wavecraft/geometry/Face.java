package org.wavecraft.geometry;

public class Face {
	// a face is represented with : 
	// - the coordinates with smallest value 
	// - the scale J
	// - normal direction ( +-1 the face is orthogonal to Ox, +-2 ortho to Oy, +-3 ortho to Oz)
	// orientation depends of sign of normal 

	public Coord3i coord;
	private int J;
	private int normal;
	private DyadicBlock father = null;

	public Face (int x,int y ,int z ,int J ,int normal){
		this.coord = new Coord3i(x, y, z);
		this.J = J;
		this.normal = normal;
	}
	public Face(Coord3i coord,int J,int normal){
		this.coord=coord;
		this.J=J;
		this.normal=normal;
	}
	public Face(Coord3i coord,int J,int normal,DyadicBlock father){
		this.coord=coord;
		this.J=J;
		this.normal=normal;
		this.father = father;
	}

	public int getJ(){
		return J;
	}
	public int getNormal(){
		return normal;
	}
	@Override public boolean equals(Object o){
		if ( !(o instanceof Face) ) return false;
		Face face2= (Face) o;
		return (coord.x == face2.coord.x && coord.y == face2.coord.y  && coord.z == face2.coord.z && J==face2.J && normal==face2.normal);
	}


	@Override public String toString(){
		return String.format("Face : (%d, %d, %d, %d, %d)",coord.x,coord.y,coord.z, J,normal);
	}

	@Override
	public int hashCode(){
		//http://www.javamex.com/tutorials/collections/hash_function_guidelines.shtml
		// all those 15485867 ... are prime
		return (coord.x*15485867)^(coord.y*15485927)^(coord.z*15485941)^(J*15485989)^(J*15485993)^(normal*15487249); 
	}





	public double  edgeLength(){
		return Math.pow(2, J);
	}

	public Face reverse(){
		return new Face(coord, J, -normal);
	}

	public Coord3d[] getVertices(){
		double ttj= edgeLength();
		Coord3d[] vertices=new Coord3d[4];
		double x=ttj*coord.x;
		double y=ttj*coord.y;
		double z=ttj*coord.z;
		switch (normal){
		case 1 : case -1 :
			vertices[0]=new Coord3d(x, y, z);
			vertices[1]=new Coord3d(x, y, z+ttj);
			vertices[2]=new Coord3d(x, y+ttj, z+ttj);
			vertices[3]=new Coord3d(x, y+ttj, z);
			break;
		case 2 : case -2 :
			vertices[0]=new Coord3d(x, y, z);
			vertices[1]=new Coord3d(x, y, z+ttj);
			vertices[2]=new Coord3d(x+ttj, y, z+ttj);
			vertices[3]=new Coord3d(x+ttj, y, z);
			break;
		case 3 : case -3 :
			vertices[0]=new Coord3d(x, y, z);
			vertices[1]=new Coord3d(x, y+ttj, z);
			vertices[2]=new Coord3d(x+ttj, y+ttj, z);
			vertices[3]=new Coord3d(x+ttj, y, z);
			break;
		}

		return vertices;
	}

	/**
	 * @return the list of the vertices in the face in
	 * the same order as method from FaceToArray
	 */
	public Coord3i[] getVerticesI(){
		int ttj= (int) edgeLength();
		Coord3i[] vertices=new Coord3i[4];
		int x=ttj*coord.x;
		int y=ttj*coord.y;
		int z=ttj*coord.z;
		switch (normal){
		case 1 : 
			vertices[0]=new Coord3i(x, y, z);
			vertices[1]=new Coord3i(x, y+ttj, z);
			vertices[2]=new Coord3i(x, y+ttj, z+ttj);
			vertices[3]=new Coord3i(x, y, z+ttj);
			break;
		case -1 :
			vertices[0]=new Coord3i(x, y, z);
			vertices[1]=new Coord3i(x, y, z+ttj);
			vertices[2]=new Coord3i(x, y+ttj, z+ttj);
			vertices[3]=new Coord3i(x, y+ttj, z);
			break;
		case 2 : 
			vertices[0]=new Coord3i(x, y, z);
			vertices[1]=new Coord3i(x, y, z+ttj);
			vertices[2]=new Coord3i(x+ttj, y, z+ttj);
			vertices[3]=new Coord3i(x+ttj, y, z);
			break;
		case -2 :
			vertices[0]=new Coord3i(x, y, z);
			vertices[1]=new Coord3i(x+ttj, y, z);
			vertices[2]=new Coord3i(x+ttj, y, z+ttj);
			vertices[3]=new Coord3i(x, y, z+ttj);
			break;
		case 3 : 
			vertices[0]=new Coord3i(x, y, z);
			vertices[1]=new Coord3i(x+ttj, y, z);
			vertices[2]=new Coord3i(x+ttj, y+ttj, z);
			vertices[3]=new Coord3i(x, y+ttj, z);
			break;
		case -3 :
			vertices[0]=new Coord3i(x, y, z);
			vertices[1]=new Coord3i(x, y+ttj, z);
			vertices[2]=new Coord3i(x+ttj, y+ttj, z);
			vertices[3]=new Coord3i(x+ttj, y, z);
			break;
		}

		return vertices;
	}


	public DyadicBlock getFather(){
		return this.father;
	}

	public double intersectionSignedDistance(Coord3d origin, Coord3d vector){
		// if the demi-line defined by the point origin and the vector vector
		// intersects the face, return the distance to it,
		// otherwise return 10E20

		// find the intersection point with the plane defined by the face
		double ttj = edgeLength();
		double x = ttj*coord.x;
		double y = ttj*coord.y;
		double z = ttj*coord.z;
		double signedDistance = 10E20;

		switch (normal) {
		case 1 : case -1 :
			if (Math.abs(vector.x)>10E-5){
				signedDistance = (x - origin.x)/vector.x;  
			}
			break;
		case 2 : case -2 :
			if (Math.abs(vector.y)>10E-5){
				signedDistance = (y - origin.y)/vector.y;  
			}
			break;
		case 3 : case -3 :
			if (Math.abs(vector.z)>10E-5){
				signedDistance = (z - origin.z)/vector.z;  
			}
			break;
		}
		Coord3d intersection = new Coord3d(vector);
		intersection.scale(signedDistance);
		intersection.add(origin);

		// look if intersection point is on the face
		boolean doesIntersect = false;
		switch (normal) {
		case 1 : case -1 :
			doesIntersect = y < intersection.y && intersection.y < y + ttj &&
			z < intersection.z && intersection.z < z + ttj ;
			break;
		case 2 : case -2 :
			doesIntersect = x < intersection.x && intersection.x < x + ttj &&
			z < intersection.z && intersection.z < z + ttj ;
			break;
		case 3 : case -3 :
			doesIntersect = x < intersection.x && intersection.x < x + ttj &&
			y < intersection.y && intersection.y < y + ttj ;
			break;
		}
		//System.out.format("face %s intersect %s signed dis %f %n",toString(), intersection.toString(),signedDistance);
		if (!doesIntersect || signedDistance<0) {
			signedDistance = 10E20;
		}
		return signedDistance;
	}

	public boolean doesIntersect(Coord3d origin, Coord3d vector){
		return intersectionSignedDistance(origin, vector)<10E20;
	}

	/**
	 * 
	 * @return the block in front of the face (not the one which owns the face, the other)
	 */
	public DyadicBlock getBlockInFrontOf() {
		DyadicBlock block = null ;
		switch (normal) {
		case -1 :
			block = new DyadicBlock(coord.x-1, coord.y, coord.z, J);
			break;

		case 1 :
			block = new DyadicBlock(coord.x, coord.y, coord.z, J);
			break;

		case -2 :
			block = new DyadicBlock(coord.x, coord.y-1, coord.z, J);
			break;

		case 2 :
			block = new DyadicBlock(coord.x, coord.y, coord.z, J);
			break;

		case -3 :
			block = new DyadicBlock(coord.x, coord.y, coord.z-1, J);
			break;

		case 3 :
			block = new DyadicBlock(coord.x, coord.y, coord.z, J);
			break;
		}
		return block;
	}

	/**
	 * @param vertice
	 * @return the four blocks adjacent to the vertice in the direction of the normal of the face
	 */
	public DyadicBlock[] inFrontOfVertice(Coord3i vertice){

		DyadicBlock[] blocks = new DyadicBlock[4];
		int ttj = (int) Math.pow(2, J);
		if (ttj==0){
			System.out.println("bug");
		}
		int xb = vertice.x/ttj;
		int yb = vertice.y/ttj;
		int zb = vertice.z/ttj;

		int id1 = 0, id2 = 0; // the identifiers of the moving variable
		switch (normal) {
		case 1: case -1:
			xb = (normal>0) ? xb : xb-1;
			id1 = 2;
			id2 = 3;
			break;
		case 2: case -2:
			yb = (normal>0) ? yb : yb-1;
			id1 = 1;
			id2 = 3;
			break;
		case 3: case -3:
			zb = (normal>0) ? zb : zb-1;
			id1 = 1;
			id2 = 2;
			break;

		default:
			throw new IllegalArgumentException("wrong normal");
		}

		blocks[0] = new DyadicBlock(xb, yb, zb, J);
		blocks[1] = new DyadicBlock(xb+((id1==1)?-1:0), 
				yb+((id1==2)?-1:0), 
				zb+((id1==3)?-1:0),
				J);
		blocks[2] = new DyadicBlock(xb+((id1==1||id2==1)?-1:0), 
				yb+((id1==2||id2==2)?-1:0), 
				zb+((id1==3||id2==3)?-1:0),
				J);
		blocks[3] = new DyadicBlock(xb+((id2==1)?-1:0), 
				yb+((id2==2)?-1:0), 
				zb+((id2==3)?-1:0),
				J);
		return blocks;
	}


}
