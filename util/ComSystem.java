package team079.util;

import battlecode.common.*;

//Basic ComSystem package, to keep the communication system working well
public class ComSystem {
	public static RobotController rc;
	public static final int ORELOG = 100;
	public static final int USELESSMINERS = 120;
	public static final int MININGLOCATIONLOG = 10000;
	private static MapLocation miningLocation;
	
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
	
	// Syncronized versions
	
	public static void sendLocationSync(int channel, int x, int y, boolean flag) throws GameActionException{
		sendLocationSync(channel, new MapLocation(x,y), flag);
	}
	
	public static void sendLocationSync(int channel, MapLocation loc, boolean flag) throws GameActionException {
		//Encode in the format fsyyysxxx
		sendLocationSync(channel, new BetterMapLocation(loc),flag);	
	}
	//Send one map location and one boolean flag
	public static void sendLocationSync(int channel, BetterMapLocation loc, boolean flag) throws GameActionException{
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
		writeSync(channel, message);	
	}

	public static MapLocation getLocationSync(int channel) throws GameActionException{
		int message = readSync(channel);
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
	
	//---------------------Average Calculation Methods-------------------------
	public static void addToAverage(int channel, int toAdd) throws GameActionException{
		//Add one to the running  tally
		writeSync(channel+2,readSync(channel+2)+1);
		//Add to the total
		writeSync(channel,readSync(channel)+toAdd);
		
	}
	
	public static int getAverage(int channel) throws GameActionException{
		//Read the current total
		int avg = readSync(channel);
		//divide by tally and return
		int div = readSync(channel+2);
		if(div == 0){
			return 0;
		}
		return avg/readSync(channel+2);
	}
	
	//------------------Sync Methods------------------------------
	//read a message from the proper channel if synced
	private static int readSync(int channel) throws GameActionException{
		//If match num is even, use the alt channel
		if(Clock.getRoundNum()%2 == 0){
			return rc.readBroadcast(channel+1);
		}else{
			return rc.readBroadcast(channel);
		}
	}
	
	//Send a message to the proper channel
	private static void writeSync(int channel, int toWrite) throws GameActionException{
		//If match num is odd, use the alt channel
		if(Clock.getRoundNum()%2 == 0){
			rc.broadcast(channel, toWrite);
		}else{
			rc.broadcast(channel+1, toWrite);
		}
	}
	
	private static int readSyncInverted(int channel) throws GameActionException{
		//If match num is even, use the alt channel
		if(Clock.getRoundNum()%2 == 0){
			return rc.readBroadcast(channel);
		}else{
			return rc.readBroadcast(channel + 1);
		}
	}
	
	//-------------------Mining Communication Methods------------------------------
	//Log the location sent if it is a better place to mine
	public static void logMiningIfBetter(int oreAtLoc, MapLocation loc) throws GameActionException{
		//Only log every ten turns
		if(oreAtLoc>=rc.readBroadcast(ORELOG)*.9){
			rc.broadcast(ORELOG, oreAtLoc);
			sendLocation(ORELOG+1, loc, false);
		}
	}

	public static MapLocation getMiningLoc() throws GameActionException{
		if(miningLocation == null || Clock.getRoundNum()%10==0){
			miningLocation = getLocation(ORELOG+1);
		}
		
		return miningLocation;
	}

	public static void clearMiningInfo() throws GameActionException{
		//Only clear log every ten turns
		rc.broadcast(ORELOG, 0);
	}
	
	public static void reportUselessMiner() throws GameActionException{
		writeSync(USELESSMINERS, readSyncInverted(USELESSMINERS)+1);
	}
	
	public static int getUselessMiners() throws GameActionException{
		return readSync(USELESSMINERS);
	}
	
	public static void clearUselessMiners() throws GameActionException{
		writeSync(USELESSMINERS, 0);
	}
	
	public static void logMined(MapLocation loc) throws GameActionException{
		BetterMapLocation betterLoc = new BetterMapLocation(loc);
		int channel = MININGLOCATIONLOG + 120*betterLoc.x + betterLoc.y;
		rc.broadcast(channel, 1);
	}
	
	public static void logOffMap(MapLocation loc) throws GameActionException{
		BetterMapLocation betterLoc = new BetterMapLocation(loc);
		int channel = MININGLOCATIONLOG + 120*betterLoc.x + betterLoc.y;
		rc.broadcast(channel, 2);
	}
	
	public static int readMiningSquare(MapLocation loc) throws GameActionException{
		BetterMapLocation betterLoc = new BetterMapLocation(loc);
		int channel = MININGLOCATIONLOG + 120*betterLoc.x + betterLoc.y;
		return rc.readBroadcast(channel);
	}
}
