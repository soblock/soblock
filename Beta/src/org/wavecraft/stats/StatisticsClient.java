package org.wavecraft.stats;

import org.wavecraft.client.Timer;
import org.wavecraft.geometry.octree.events.OctreeEventMediator;


// this class encoded all statistics related to client
// singleton
public class StatisticsClient {
	private static StatisticsClient statisticsClient = null;

	private static Graph timeGraph;
	private static Graph eventLogCount;
	private static Graph fpsGraph;

	private StatisticsClient(){
		timeGraph = new Graph(512);
		fpsGraph = new Graph(512);
		eventLogCount = new Graph(512);
	}



	public static StatisticsClient getStatisticsClient(){
		if (statisticsClient == null){
			statisticsClient = new StatisticsClient();
		}
		return statisticsClient;
	}

	public static void update(){
		timeGraph.putValueAtTime(Timer.getDt(),Timer.getCurrT());
		int fpsWindow = 4;
		double fpsWindowed = 1000*fpsWindow/(timeGraph.getTime(1)- timeGraph.getTime(fpsWindow+1));
		fpsGraph.putValueAtTime(fpsWindowed, Timer.getCurrT());
		eventLogCount.putValueAtTime(Math.log10(OctreeEventMediator.eventCount()+1), Timer.getCurrT());
	}

	public static Graph getTimeGraph(){
		return StatisticsClient.timeGraph;
	}
	
	public static Graph getFpsGraph(){
		return StatisticsClient.fpsGraph;
	}

	public static Graph getEventCountGraph(){
		return StatisticsClient.eventLogCount;
	}
}
