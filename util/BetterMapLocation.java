package team079.util;

import battlecode.common.*;

//Currently Defunct class.
public class BetterMapLocation {
	public final int x;
	public final int y;
	private static MapLocation zeroLoc;
	
	public BetterMapLocation(MapLocation loc){
		x = loc.x-zeroLoc.x;
		y = loc.y-zeroLoc.y;
	}
	
	public BetterMapLocation(int x, int y){
		this.x = x;
		this.y = y;
	}

	public static void init(RobotController rc){
		zeroLoc = rc.senseHQLocation();		
	}
	
	public MapLocation toMapLoc(){
		int mapX = zeroLoc.x + x;
		int mapY = zeroLoc.y + y;
		return new MapLocation(mapX, mapY);
	}

}
