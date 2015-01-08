package team079;

import battlecode.common.*;

public class ComSystem {
	public static RobotController rc;
	
	public static void init(RobotController rcin){
		rc = rcin;
	}
	public static void sendLocation(int channel, int x, int y, boolean flag) throws GameActionException{
		sendLocation(channel, new MapLocation(x,y), flag);
	}
	
	public static void sendLocation(int channel, MapLocation loc, boolean flag) throws GameActionException {
		int message = loc.x;
		message += loc.y*1000;
		if(flag){
			message += 1000000;
		}
		rc.broadcast(channel, message);		
	}
	
	public static MapLocation getLocation(int channel) throws GameActionException{
		int message = rc.readBroadcast(channel);
		return new MapLocation(message%1000,(message%1000000)/1000);
	}	

}
