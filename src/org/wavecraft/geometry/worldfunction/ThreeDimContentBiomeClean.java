package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.blocktree.Terran;

public class ThreeDimContentBiomeClean implements ThreeDimContent {


	private final ThreeDimFunctionPerlinMS humidityRandomness  = new ThreeDimFunctionPerlinMS(7,256,0,1);
	private final ThreeDimFunctionPerlinMS temperatureRandomness = new ThreeDimFunctionPerlinMS(7,256,0,1, 1234567890);
	private final ThreeDimFunctionPerlinMS soilRandomness = new ThreeDimFunctionPerlinMS(3,256,0,1, 23456789);
	private final ThreeDimFunction terranAltitude;

	private final double depthGroundSubsoilInterface; // depth of the interface between the ground and the down layer
	private final double zMin;
	private final double zMax;


	public ThreeDimContentBiomeClean(ThreeDimFunction terranAltitude,
			double depthGroundSubsoilInterface,
			double zMin, 
			double zMax){
		this.terranAltitude = terranAltitude;
		this.depthGroundSubsoilInterface = depthGroundSubsoilInterface;
		this.zMin = zMin;
		this.zMax = zMax;
	}

	@Override
	public Terran contentAt(DyadicBlock block) {

		if (absoluteDepth(block) < - depthGroundSubsoilInterface ){ // we are in the sub soils
			double d = soilRandomness.valueAt(block.center());
			if (d<0.8){
				return Terran.NAT_STONE;
			} else {
				return Terran.NAT_SAND;
			}
		}
		else { // near interface between the ground and the air

			double humidity = humdityAtBlock(block);
			double temperature = temperatureAtBlock(block);
			Climate climate = Climate.getClimate(humidity, temperature);
			double d = soilRandomness.valueAt(block.center());
			switch (climate) {
			case ARTIC:
				if (d<0.4 ){
					return Terran.NAT_GROUNDSNOW;
				} else  if (d<0.6) {
					return Terran.NAT_GROUNDICE;
				} else {
					return Terran.NAT_ICE;
				}

			case DESERT:
				if (d<0.5){
					return Terran.NAT_SAND;
				} else {
					return Terran.NAT_SOLIDSAND;
				}

			case JUNGLE:
				if (d<0.5) {
					return Terran.NAT_GRASS;
				} else {
					return Terran.NAT_HERBY_ROCK;
				}
				
			case FOREST:
				if (d<0.5) {
					return Terran.NAT_GRASS;
				} else {
					return Terran.NAT_HERBY_ROCK;
				}
			default:
				break;
			}

			//			for debug
			//
			//			switch (climate) {
			//			case ARTIC:
			//				return Terran.NAT_SNOW;
			//			case DESERT:
			//				return Terran.NAT_SAND;
			//			case FOREST: 
			//				return Terran.NAT_GRASS;
			//			case JUNGLE:
			//				return Terran.NAT_DIRT;
			//			default:
			//				break;
			//			}

		}
		return null;
	}

	/**
	 * 
	 * @param block
	 * @return normalized temperature between 0 and 1 
	 */
	private double temperatureAtBlock(DyadicBlock block){
		// half random, half cold when you get up
		double temperature = 0.5 * temperatureRandomness.valueAt(block.center());
		temperature += 0.5 * (1-relativeAltitude(block));
		return temperature;
	}

	/**
	 * 
	 * @param block
	 * @return normalized humidity between 0 and 1
	 */
	private double humdityAtBlock(DyadicBlock block){

		double humidty = humidityRandomness.valueAt(block.center());
		if (humidty<0 || humidty>1){
			throw new RuntimeException();
		}
		return humidty;
	}

	/**
	 * 
	 * @param block
	 * @return normalized altitude (relatively to z=0) between 0 and 1
	 */
	private double relativeAltitude(DyadicBlock block){
		return (block.center().z-zMin)/(zMax - zMin);
	}

	/**
	 * 
	 * @param block
	 * @return absolute depth (relatively to the z of the terran) 
	 */
	private double absoluteDepth(DyadicBlock block){
		return terranAltitude.valueAt(block.center());
	}

}
