package org.wavecraft.geometry;


public class BoundingBox {
	private Coord3d minCoords;
	private Coord3d maxCoords;



	public BoundingBox(Coord3d minCoords, Coord3d maxCoords){
		this.minCoords = minCoords;
		this.maxCoords = maxCoords;
	}

	public BoundingBox(DyadicBlock block){
		double ttj = block.edgeLentgh();
		this.minCoords = new Coord3d(block.x*ttj, block.y*ttj, block.z*ttj);
		this.maxCoords = new Coord3d(block.x*ttj+ttj, block.y*ttj+ttj, block.z*ttj+ttj);
	}


	public BoundingBox extrude(Coord3d vector){
		double xmin,ymin,zmin,xmax,ymax,zmax;
		xmin = Math.min(minCoords.x + vector.x,minCoords.x);
		ymin = Math.min(minCoords.y + vector.y,minCoords.y);
		zmin = Math.min(minCoords.z + vector.z,minCoords.z);
		xmax = Math.max(maxCoords.x + vector.x,maxCoords.x);
		ymax = Math.max(maxCoords.y + vector.y,maxCoords.y);
		zmax = Math.max(maxCoords.z + vector.z,maxCoords.z);
		Coord3d minCoords2 = new Coord3d(xmin,ymin,zmin);
		Coord3d maxCoords2 = new Coord3d(xmax,ymax,zmax);
		return new BoundingBox(minCoords2,maxCoords2);

	}
	public Coord3d getMinCoord3d(){
		return this.minCoords;
	}

	public Coord3d getMaxCoord3d(){
		return this.maxCoords;
	}

	// coord is between 0,1
	public Coord3d relativeCoord3d(Coord3d coord){
		double x = minCoords.x + coord.x * (maxCoords.x - minCoords.x);
		double y = minCoords.y + coord.y * (maxCoords.y - minCoords.y);
		double z = minCoords.z + coord.z * (maxCoords.z - minCoords.z);
		return new Coord3d(x, y, z);
	}


	public BoundingBox relativeBoundingBox(BoundingBox box){
		return new BoundingBox(relativeCoord3d(box.minCoords), relativeCoord3d(box.maxCoords));
	}

	@Override 
	public String toString(){
		return minCoords.toString() + " / " + maxCoords.toString();
	}

	public boolean intersects(BoundingBox box){
		// two bounding box does not intersect if at least one of 0x,0y,0z direction
		// separates them
		// or : one can be contained in the other
		return   !(maxCoords.x < box.minCoords.x || box.maxCoords.x < minCoords.x 
				|| maxCoords.y < box.minCoords.y || box.maxCoords.y < minCoords.y
				|| maxCoords.z < box.minCoords.z || box.maxCoords.z < minCoords.z) || 
				(minCoords.x < box.minCoords.x && maxCoords.x > box.maxCoords.x 
				&& minCoords.y < box.minCoords.y && maxCoords.y > box.maxCoords.y
				&& minCoords.z < box.minCoords.z && maxCoords.z > box.maxCoords.z);
	}


	public boolean intersects(DyadicBlock block){
		BoundingBox box = new BoundingBox(block);
		return intersects(box);
	}

	public BoundingBox translate(Coord3d trans){
		return new BoundingBox(minCoords.translate(trans), maxCoords.translate(trans));
	}

}
