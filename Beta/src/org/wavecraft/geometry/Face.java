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

	public Coord3d[] vertices(){
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

	public DyadicBlock getNeighbor() {
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
	
	
}
