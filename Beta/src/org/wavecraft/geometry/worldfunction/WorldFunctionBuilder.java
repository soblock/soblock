package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;

// this class builds world functions for end-user
// every construction of world functions should happen HERE
public class WorldFunctionBuilder {
	public static WorldFunction getWorldFunctionFlatUniform(double z0){
		return new WorldFunctionWrapper(new ThreeDimContentConstant(), new ThreeDimFunctionFlat(z0));
	}
	
	public static WorldFunction getWorldFunctionSphereUniform(Coord3d center, double radius){
		return new WorldFunctionWrapper(new ThreeDimContentConstant(), new ThreeDimFunctionSphere(center, radius));
	}
	
	public static WorldFunction getWorldFunctionSincUniform(Coord3d center,double scale, double deltaz,double Z0){
		return new WorldFunctionWrapper(new ThreeDimContentConstant(),new ThreeDimFunctionSinc(center, scale, deltaz, Z0));
	}
	
	public static WorldFunction getWorldFunctionPerlinUniform(){
		return new WorldFunctionWrapper(new ThreeDimContentConstant(), new ThreeDimFunctionPerlin());
	}
	
	public static WorldFunction getWorldFunctionPerlinMSUniform(){
		return new WorldFunctionWrapper(new ThreeDimContentConstant(), new ThreeDimFunctionPerlinMS());
	}
	
	public static WorldFunction getWorldFunctionNoisyFlastNoisyContent(double z0,double deltaz, int J){
		ThreeDimFunction function = new ThreeDimFunctionNoisyFlat(z0,deltaz);
		//ThreeDimContent content = new ThreeDimContentBiome(z0, zmax, function, J);
		ThreeDimContent content = new ThreeDimContentBiomeClean(function, 20, z0-deltaz/2, z0+deltaz/2);
		return new WorldFunctionWrapper(content, function);
	}
}
