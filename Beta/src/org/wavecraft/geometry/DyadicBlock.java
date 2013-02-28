package org.wavecraft.geometry;

import org.wavecraft.Soboutils.MathSoboutils;
import org.wavecraft.geometry.octree.Octree;



@SuppressWarnings("serial")
public class DyadicBlock extends Coord3i {
	private int J;

	public DyadicBlock(int x,int y,int z,int J){
		super(x,y,z);
		this.J = J;
	}

	public double distanceSquared(Coord3d pos){
		double ttj = Math.pow(2, J);
		double dx = (ttj*(x+0.5) - pos.x);
		double dy = (ttj*(y+0.5) - pos.y);
		double dz = (ttj*(z+0.5) - pos.z);
		return dx*dx + dy*dy + dz*dz;
	}

	public double distance(Coord3d pos){
		double ttj = Math.pow(2, J);
		double dx = (ttj*(x+0.5) - pos.x);
		double dy = (ttj*(y+0.5) - pos.y);
		double dz = (ttj*(z+0.5) - pos.z);
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	public int getJ() {
		return J;
	}

	public double edgeLentgh(){
		return Math.pow(2, J);
	}

	public Coord3d center(){
		double ttj = edgeLentgh();
		return new Coord3d(this.x*ttj + ttj/2, this.y*ttj + ttj/2, this.z*ttj + ttj/2);
	}

	public Coord3d[] vertices(){
		Coord3d[] v = new Coord3d[8];
		double ttj = Math.pow(2, J);
		v[0] = new Coord3d(x*ttj,     y*ttj,     z*ttj);
		v[1] = new Coord3d((x+1)*ttj, y*ttj,     z*ttj);
		v[2] = new Coord3d(x*ttj,     (y+1)*ttj, z*ttj);
		v[3] = new Coord3d((x+1)*ttj, (y+1)*ttj, z*ttj);
		v[4] = new Coord3d(x*ttj,     y*ttj,     (z+1)*ttj);
		v[5] = new Coord3d((x+1)*ttj, y*ttj,     (z+1)*ttj);
		v[6] = new Coord3d(x*ttj,     (y+1)*ttj, (z+1)*ttj);
		v[7] = new Coord3d((x+1)*ttj, (y+1)*ttj, (z+1)*ttj);
		return v;
	}

	public DyadicBlock subBlock(int offset){
		DyadicBlock block;
		switch (offset) {
		case 0:
			block = new DyadicBlock(2*x, 2*y, 2*z, getJ()-1);
			break;
		case 1:
			block = new DyadicBlock(2*x+1, 2*y, 2*z, getJ()-1);
			break;
		case 2:
			block = new DyadicBlock(2*x, 2*y+1, 2*z, getJ()-1);
			break;
		case 3:
			block = new DyadicBlock(2*x+1, 2*y+1, 2*z, getJ()-1);
			break;
		case 4:
			block = new DyadicBlock(2*x, 2*y, 2*z+1, getJ()-1);
			break;
		case 5:
			block = new DyadicBlock(2*x+1, 2*y, 2*z+1, getJ()-1);
			break;
		case 6:
			block = new DyadicBlock(2*x, 2*y+1, 2*z+1, getJ()-1);
			break;
		case 7:
			block = new DyadicBlock(2*x+1, 2*y+1, 2*z+1, getJ()-1);
			break;
		default:
			block = null;
			try {
				throw new IllegalArgumentException("Offset should be between 0 and 7");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

			break;
		}
		return block;
	}

	public int offset(){
		return 4*(this.z%2) + 2*(this.y%2) + (this.x%2);
	}

	public String toString(){
		return String.format("DyadicBlock : (%d, %d, %d, %d)", x,y,z,J);
	}

	@Override
	public int hashCode(){
		//http://www.javamex.com/tutorials/collections/hash_function_guidelines.shtml
		// all those 15485867 ... are prime
		return (x*15485867)^(y*15485927)^(z*15485941)^(J*15485989); 
	}

	@Override
	public boolean equals(Object o){
		if (o instanceof DyadicBlock){
			DyadicBlock o2 = (DyadicBlock) o;
			return (x == o2.x && y == o2.y && z == o2.z && J==o2.J);
		}
		else
			return false;
	}

	public Face[] getFaces(){
		Face[] faces = new Face[6];
		Coord3i coord = new Coord3i(x, y, z);
		faces[0] = new Face(coord, J, -1,this);
		coord = new Coord3i(x, y, z);
		faces[1] = new Face(coord, J, -2,this);
		coord = new Coord3i(x, y, z);
		faces[2] = new Face(coord, J, -3,this);
		coord = new Coord3i(x + 1, y, z);
		faces[3] = new Face(coord, J, 1,this);
		coord = new Coord3i(x , y + 1, z);
		faces[4] = new Face(coord, J, 2,this);
		coord = new Coord3i(x , y , z + 1);
		faces[5] = new Face(coord, J, 3,this);
		return faces;
	}

	public double nearestIntersectedFaceDistance(Coord3d origin, Coord3d vector){
		Face[] faces = getFaces();
		double dmin = 10E20;
		for (int i = 0; i<6; i++){
			double dcurr = faces[i].intersectionSignedDistance(origin, vector);
			if (dcurr<dmin){
				dmin = dcurr;
			}
		}
		return dmin;
	}

	public Face nearestIntersectedFace(Coord3d origin, Coord3d vector){
		Face[] faces = getFaces();
		double dmin = 10E20;
		int imin = -1;
		for (int i = 0; i<6; i++){
			double dcurr = faces[i].intersectionSignedDistance(origin, vector);
			if (dcurr<dmin){
				dmin = dcurr;
				imin = i;
			}
		}
		if (imin>=0){
			return faces[imin];
		}
		else{
			return null;
		}
	}


	public boolean doesIntersectLine(Coord3d origin, Coord3d vector){
		return nearestIntersectedFaceDistance(origin, vector)<10E20;
	}

	public DyadicBlock neighbor(int dx,int dy,int dz){
		int maxn = (int)(Math.pow(2, Octree.JMAX - J ));
		int xn = x + dx;
		int yn = y + dy;
		int zn = z + dz;
		if (0 <= xn && xn< maxn && 
				0 <= yn && yn< maxn &&
				0 <= zn && zn< maxn){
			return new DyadicBlock(xn, yn, zn, J);
		}
		else {
			return null;
		}
	}

	public DyadicBlock[] sixNeighbors(){
		DyadicBlock[] neighbors = new DyadicBlock[6];
		neighbors[0] = neighbor(-1, 0, 0);
		neighbors[1] = neighbor(1, 0, 0);
		neighbors[2] = neighbor(0, 1, 0);
		neighbors[3] = neighbor(0, -1, 0);
		neighbors[4] = neighbor(0, 0, 1);
		neighbors[5] = neighbor(0, 0, -1);
		return neighbors;
	}

	public DyadicBlock ancestor(int J){
		if (J < this.J){
			return null;
		}
		else{
			int div = (int)(Math.pow(2, J-this.J));
			return new DyadicBlock(x/div, y/div, z/div, J);
		}
	}

	public boolean isAdjacentTo(DyadicBlock block){
		DyadicBlock biggerBlock =  (block.getJ()< this.getJ())?this:block;
		DyadicBlock smallerBlock = (block.getJ()>=this.getJ())?this:block;
		int step = (int) Math.pow(2, biggerBlock.getJ()- smallerBlock.getJ());
		// + step or -1
		int offsetx = smallerBlock.x - step*biggerBlock.x;
		int offsety = smallerBlock.y - step*biggerBlock.y;
		int offsetz = smallerBlock.z - step*biggerBlock.z;
		int validOffset = 0;
		if (offsetx == -1 || offsetx == step) validOffset++;
		if (offsety == -1 || offsety == step) validOffset++;
		if (offsetz == -1 || offsetz == step) validOffset++;
		int zeroOffset = 0;
		if (0 <= offsetx && offsetx < step ) zeroOffset++;
		if (0 <= offsety && offsety < step ) zeroOffset++;
		if (0 <= offsetz && offsetz < step ) zeroOffset++;
		return (validOffset == 1 && zeroOffset == 2);
	}

	public boolean contains(DyadicBlock block){
		if (this.getJ() < block.getJ()) {
			return false;
		}
		int step = (int) Math.pow(2, this.getJ()- block.getJ());
		int xDiv = MathSoboutils.divideFloor(block.x, step);
		int yDiv = MathSoboutils.divideFloor(block.y, step);
		int zDiv = MathSoboutils.divideFloor(block.z, step);
		return (xDiv == this.x &&
				yDiv == this.y && 
				zDiv == this.z );
	}

	/**
	 * @param block
	 * @return the sons'id that contain the block or -1 otherwise
	 */
	public int findSonContaining(DyadicBlock block) {
		// check that block is a subblock of me
		if (this.contains(block)){
			return MathSoboutils.ithbit(block.x, this.getJ() - block.getJ()) + 2
					* MathSoboutils.ithbit(block.y, this.getJ() - block.getJ())
					+ 4
					* MathSoboutils.ithbit(block.z, this.getJ() - block.getJ());
		} 
		else {
			return -1;
		}
	}

}
