package team079.util;

import battlecode.common.*;

//TODO See if there is any way to condsen
public class ComSystem {
	public static RobotController rc;
	
	public static void init(RobotController rcin){
		rc = rcin;
	}
	
	//----------------------------Location sending methods-------------------------------------
	public static void sendLocation(int channel, int x, int y, boolean flag) throws GameActionException{
		sendLocation(channel, new MapLocation(x,y), flag);
	}
	
	public static void sendLocation(int channel, MapLocation loc, boolean flag) throws GameActionException {
		//Encode in the format fsyyysxxx
		sendLocation(channel, new BetterMapLocation(loc),flag);	
	}
	//Send one map location and one boolean flag
	public static void sendLocation(int channel, BetterMapLocation loc, boolean flag) throws GameActionException{
		//Encode in the format fsyyysxxx
		int message = Math.abs(loc.x);
		message += Math.abs(loc.y)*10000;
		if(loc.x<0){
			message += 1000;
		}
		if(loc.y<0){
			message += 10000000;
		}
		if(flag){
			message += 100000000;
		}
		rc.broadcast(channel, message);	
	}

	public static MapLocation getLocation(int channel) throws GameActionException{
		int message = rc.readBroadcast(channel);
		//Decode in the format fsyyysxxx
		int x;
		int y;
		if((message/1000)%10 != 0){
			x = -message%1000;
		}else{
			x = message%1000;
		}
		message = message/10000;
		if((message/1000)%10 != 0){
			y = -message%1000;
		}else{
			y = message%1000;
		}
		return new BetterMapLocation(x,y).toMapLoc();
	}	
	
	//-------------------Mining Communication Methods------------------------------
	public static void writeMiningInfo(MapLocation loc, double ore) throws GameActionException{
		BetterMapLocation bLoc = new BetterMapLocation(loc);
		int channel = 50000+bLoc.x*120+bLoc.y;
		int iOre = (int)(ore*10);
		rc.broadcast(channel, iOre);		
	}
	public static void writeminingInfo(MapLocation loc) throws GameActionException{
		BetterMapLocation bLoc = new BetterMapLocation(loc);
		int channel = 50000+bLoc.x*120+bLoc.y;
		rc.broadcast(channel, 10000001);
	}
	public static boolean readHasBeenMined(MapLocation loc) throws GameActionException{
		BetterMapLocation bLoc = new BetterMapLocation(loc);
		int channel = 50000+bLoc.x*120+bLoc.y;
		if(rc.readBroadcast(channel) == 10000001){
			return true;
		}else{
			return false;
		}
	}
	public static double readOre(MapLocation loc) throws GameActionException{
		BetterMapLocation bLoc = new BetterMapLocation(loc);
		int channel = 50000+bLoc.x*120+bLoc.y;
		return((double)rc.readBroadcast(channel));
	}

}
