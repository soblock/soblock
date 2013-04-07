package org.wavecraft.modif;



import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.Coord3i;
import org.wavecraft.Soboutils.MathSoboutils;
import org.wavecraft.geometry.blocktree.Blocktree;
import org.wavecraft.geometry.blocktree.Terran;




@SuppressWarnings("serial")
public class ModifOctree extends DyadicBlock {
	public ModifOctree[] sons = null;
	public ModifOctree father = null;
	//public static int JMAX = Blocktree.JMAX;
	public double value;
	public double sumAncestors;
	public double boundMin;
	public double boundMax;
	public Terran content;

	public ModifOctree(int i, int j, int k, int J, Terran cont, double val) {
		super(i, j, k, J);
		value = val;
		content = cont;
		boundMin = boundMax = 0;
		init();
	}
	public ModifOctree(int i, int j, int k, int J, Terran cont, double val,double sum) {
		super(i, j, k, J);
		value = val;
		content = cont;
		sumAncestors=sum;
		boundMin = boundMax = 0;
		init();
	}

	public ModifOctree(Coord3i coords, int J, Terran cont, double val) {
		super(coords.x, coords.y, coords.z, J);
		content = cont;
		value = val;
		sumAncestors = 0;
		boundMin = boundMax = 0;
		init();
	}

	private void init(){
		this.computeBounds();
		this.sumAncestors = 0;
		this.computeSumAncestors();
	}
	
	public void computeBounds() {
		boundMin = 0.0;
		boundMax = 0.0;
		if (sons != null) {
			for (int offset = 0; offset < 8; offset++) {
				if (sons[offset] != null) {
					sons[offset].computeBounds();
					boundMax = sons[offset].boundMax + sons[offset].value;
					boundMin = sons[offset].boundMin + sons[offset].value;
				}
			}
			for (int offset = 0; offset < 8; offset++) {
				if (sons[offset] != null) {
					boundMax = Math.max(boundMax, sons[offset].boundMax
							+ sons[offset].value);
					boundMin = Math.min(boundMin, sons[offset].boundMin
							+ sons[offset].value);
				}
				else{
					boundMax = Math.max(boundMax, 0.);
					boundMin = Math.min(boundMin,0.);
				}
			}
		}
	}

	public void computeSumAncestors() {
	    if (father == null) sumAncestors = 0;
		if (sons != null) {
			for (int offset = 0; offset < 8; offset++) {
				if (sons[offset] != null) {
					sons[offset].sumAncestors = value + sumAncestors;
					sons[offset].computeSumAncestors();
				}
			}

		}
	}

	public int findSonContainingBlock(DyadicBlock block) {
		return MathSoboutils.ithbit(block.x, this.getJ() - block.getJ()) + 
		2 * MathSoboutils.ithbit(block.y, this.getJ() - block.getJ())
		+ 4 * MathSoboutils.ithbit(block.z, this.getJ() - block.getJ());

	}

	public double maxValueAtFrRoot(DyadicBlock block) {
		ModifOctree cell = smallestCellContainingBlock(block);
		if (cell.sons == null) {
			return cell.sumAncestors + cell.value;
		} else {
			if (cell.getJ() == block.getJ())
				return cell.sumAncestors + cell.value + cell.boundMax;
			else {
				return cell.sumAncestors + cell.value;
			}
		}
	}

	public double minValueAtFrRoot(DyadicBlock block) {
		ModifOctree cell = smallestCellContainingBlock(block);
		if (cell.sons == null)
			return cell.sumAncestors + cell.value;
		else {
			if (cell.getJ() == block.getJ())
				return cell.sumAncestors + cell.value + cell.boundMin;
			else
				return cell.sumAncestors + cell.value;
		}
	}

	public ModifOctree smallestCellContainingBlock(DyadicBlock block) {
		if (this.getJ() == block.getJ() || sons == null)
			return this;
		else {
			int offset = findSonContainingBlock(block);
			if (offset < 0 || sons[offset] == null)
				return this;
			else
				return sons[offset].smallestCellContainingBlock(block);
		}
	}
	
	private ModifOctree smallestNegativeCellContainingBlockRec(DyadicBlock block,ModifOctree save) {
		if (this.value<0){
			save = this;
		}
		if (this.getJ() == block.getJ() || sons == null)
			return save;
		else {
			int offset = findSonContainingBlock(block);
			if (offset < 0 || sons[offset] == null)
				return save;
			else
				return sons[offset].smallestNegativeCellContainingBlockRec(block,save);
		}
	}
	
	public ModifOctree smallestNegativeCellContainingBlock(DyadicBlock block) {
		return smallestNegativeCellContainingBlockRec(block,null);
	}

	public double jumpMax(DyadicBlock b) {
		int size_max = (int) Math.pow(2,Blocktree.JMAX - b.getJ());
		double jumpMax = 0;

		double vMin = minValueAtFrRoot(b);
		DyadicBlock neigh = new DyadicBlock(b.x, b.y, b.z, b.getJ());

		neigh.x += 1;
		if (neigh.x < size_max)
			jumpMax = Math.max(jumpMax, maxValueAtFrRoot(neigh) - vMin);
		neigh.x -= 2;
		if (neigh.x >= 0)
			jumpMax = Math.max(jumpMax, maxValueAtFrRoot(neigh) - vMin);
		neigh.x += 1;

		neigh.y += 1;
		if (neigh.y < size_max)
			jumpMax = Math.max(jumpMax, maxValueAtFrRoot(neigh) - vMin);
		neigh.y -= 2;
		if (neigh.y >= 0)
			jumpMax = Math.max(jumpMax, maxValueAtFrRoot(neigh) - vMin);
		neigh.y += 1;

		neigh.z += 1;
		if (neigh.z < size_max)
			jumpMax = Math.max(jumpMax, maxValueAtFrRoot(neigh) - vMin);
		neigh.z -= 2;
		if (neigh.z >= 0)
			jumpMax = Math.max(jumpMax, maxValueAtFrRoot(neigh) - vMin);
		neigh.z += 1;

		return jumpMax;

	}


	public void addModif(DyadicBlock block,double val,Terran content){
		if (this.getJ()>block.getJ()){
			int ind = findSonContainingBlock(block);
			if (this.sons == null){
				sons = new ModifOctree[8];
			}
			double sumAncestorOfMySons = sumAncestors+this.value;
			if (this.getJ() == block.getJ()+1){ // we find it
				double valueOfMySon = val-sumAncestorOfMySons;
				sons[ind]= new ModifOctree(
						this.subBlock(ind).x, 
						this.subBlock(ind).y, 
						this.subBlock(ind).z,this.getJ()-1,content, valueOfMySon, sumAncestorOfMySons);
				sons[ind].father = this; 				
			}
			else {
				if (sons[ind] == null){
					sons[ind]= new ModifOctree(
							this.subBlock(ind).x, 
							this.subBlock(ind).y, 
							this.subBlock(ind).z,this.getJ()-1,null,0, sumAncestorOfMySons);
					sons[ind].father = this;
				}
			
				sons[ind].addModif(block, val, content);
				
			}

		}
	}
	
	@Override
	public String toString() {
		return "ModifOctree [value=" + value + ", sumAncestors=" + sumAncestors
				+ ", content=" + content + ", x=" + x + ", y=" + y + ", z=" + z
				+ ", getJ()=" + getJ() + "]";
	}
}
