package org.wavecraft.geometry.worldfunction;

import org.wavecraft.geometry.Coord3d;
import org.wavecraft.geometry.Coord3i;
import org.wavecraft.geometry.DyadicBlock;
import org.wavecraft.geometry.octree.Octree;

public class ThreeDimContentBiome implements ThreeDimContent{

	
	// TODO ::
	// rescale humidity and temperature
	private ThreeDimFunction function;
	@Override
	public int contentAt(DyadicBlock block) {
		// TODO Auto-generated method stub
		return content(block.center(), function);
	}



	//Perlin_Multiscale_laurent humidity;
	//Perlin_Multiscale_laurent temperature;
	// perlin map to choose between the two diff soils
	//Perlin_Multiscale_laurent soil;
	
	public ThreeDimFunctionPerlinMS humidity;
	public ThreeDimFunctionPerlinMS temperature;
	public ThreeDimFunctionPerlinMS soil;
	/// climates table. temperature for the first indice, humidity for the second one
	int [][]climates;
	/// proportion of the first soil
	double [] soils_proportions;
	int [][]soils;
	int []subsoils;
	int []soil_depth;
	double g_over_R=100;
	double T0=10;
	double z0;
	double dT=0;
	double Tmin=-40;
	double Tmax=Tmin+75;
	//size of the smallest bloc in meter: if the smallest bloc is 1m*1m*1m, then on a level 14 
	// (altitude between -8000 and +8000), since everything is metric system (m,s,Kg,J...)
	// it should be realistic..
	// so maybe the following is ok
	double size_bloc=Math.pow(2,14-Octree.JMAX);

	public ThreeDimContentBiome(double Z0,double Zmax, ThreeDimFunction function){
		g_over_R=9.81/287.105;
		z0=Z0;
		initialize_Perlin_maps();
		initialize_soils();
		initialize_subsoils();
		initialize_soil_depth();
		initialize_soils_proportions();
		initialize_climates();
		this.function = function;
	}

	public void initialize_Perlin_maps(){
		//humidity= new Perlin_Multiscale_laurent(2,256,16*8);
		//temperature= new Perlin_Multiscale_laurent(2,256,16*8);
		//soil= new Perlin_Multiscale_laurent(4,256,16*8);
		humidity = new ThreeDimFunctionPerlinMS(5,256,0,4);
		temperature = new ThreeDimFunctionPerlinMS(5,256,Tmin,Tmax);
		soil = new ThreeDimFunctionPerlinMS(4,256);
	}
	public void initialize_subsoils(){
		subsoils=new int[11];
		subsoils[0] =6; 
		subsoils[1] =6; 
		subsoils[2] =6; 
		subsoils[3] =6; 
		subsoils[4] =6; 
		subsoils[5] =7; 
		subsoils[6] =7; 
		subsoils[7] =7; 
		subsoils[8] =7; 
		subsoils[9] =7; 
		subsoils[10]=7; 

	}
	public void initialize_soil_depth(){
		soil_depth=new int[11];
		soil_depth[0] =6; 
		soil_depth[1] =6; 
		soil_depth[2] =6; 
		soil_depth[3] =6; 
		soil_depth[4] =6; 
		soil_depth[5] =6; 
		soil_depth[6] =6; 
		soil_depth[7] =6; 
		soil_depth[8] =6; 
		soil_depth[9] =6; 
		soil_depth[10]=6; 

	}
	public void initialize_soils(){
		soils=new int[2][11];
		soils[0][0] =1; soils[1][0] =1;
		soils[0][1] =1; soils[1][1] =2;
		soils[0][2] =1; soils[1][2] =3;
		soils[0][3] =1; soils[1][3] =4;
		soils[0][4] =2; soils[1][4] =5;
		soils[0][5] =2; soils[1][5] =6;
		soils[0][6] =2; soils[1][6] =2;
		soils[0][7] =2; soils[1][7] =2;
		soils[0][8] =3; soils[1][8] =2;
		soils[0][9] =4; soils[1][9] =2;
		soils[0][10]=11; soils[1][10]=11;

	}
	public void initialize_soils_proportions(){
		/// 50/50 for now
		soils_proportions=new double[11];
		for (int i=0; i<11; i++){
			soils_proportions[i]=0.;	
		}
	}
	public void initialize_climates(){
		climates=new int [5][4];
		climates[0][0]=0; climates[0][1]=1;climates[0][2]=2;climates[0][3]=3;
		climates[1][0]=4; climates[1][1]=5;climates[1][2]=6;climates[1][3]=7;
		for (int i=0; i<4; i++){
			climates[2][i]=8; climates[3][i]=9; climates[4][i]=10;
		}
	}
	public int find_climate(double h, double t){
		int h_index=(int) (h);
		int t_index=4-(int) ((t-Tmin)/15);
		h_index = Math.max(0, Math.min(h_index,3));
		t_index =Math.max(0, Math.min(t_index,4));
		return climates[t_index][h_index];
	}

	public int content(Coord3d c,ThreeDimFunction phi){
		//int climate=find_climate(humidity.val(c.x,c.y),T0-g_over_R*(c.z-z0-phi.valueAt(c))+dT*temperature.valueAt(c));
		int climate=find_climate(humidity.valueAt(new Coord3d(c.x, c.y, 0)),T0-g_over_R*size_bloc*(c.z-z0-phi.valueAt(c))+temperature.valueAt(c));
		if (-phi.valueAt(c)<soil_depth[climate]){
			if (soil.valueAt(c)<=soils_proportions[climate]) {
				return soils[0][climate];
			}
			else return soils[0][climate];
		}
		else{
			return subsoils[climate];
		}
	}

}

