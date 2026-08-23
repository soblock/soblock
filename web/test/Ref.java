import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.worldfunction.*;

public class Ref {
	public static void main(String[] a){
		WorldFunction wf = WorldFunctionBuilder.getWorldFunctionNoisyFlastNoisyContent(Math.pow(2, 11), 50, 10);
		// sample world function values
		java.util.Random r = new java.util.Random(42);
		for (int i=0;i<20;i++){
			double x = 2048 + r.nextInt(400)-200;
			double y = 2048 + r.nextInt(400)-200;
			double z = 2048 + r.nextInt(400)-200;
			System.out.printf("V %.1f %.1f %.1f %.10f%n", x,y,z, wf.valueAt(new Coord3d(x,y,z)));
		}
		// content at blocks
		for (int i=0;i<20;i++){
			int x = 2048 + r.nextInt(200)-100;
			int y = 2048 + r.nextInt(200)-100;
			int z = 2000 + r.nextInt(200)-100;
			DyadicBlock b = new DyadicBlock(x,y,z,0);
			System.out.printf("C %d %d %d %s%n", x,y,z, wf.contentAt(b));
		}
		// uncertainty bounds
		for (int J=0;J<12;J++){
			DyadicBlock b = new DyadicBlock(1,1,1,J);
			System.out.printf("U %d %.10f%n", J, wf.uncertaintyBound(b));
		}
	}
}
