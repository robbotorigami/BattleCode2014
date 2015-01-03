package team210;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TerrainTile;

public class HQ{
	public static RobotController rc;
	public static Direction allSpawningDirections[] = Direction.values();
	public static int[] typePerChannel = {};
	public static int[] squadOneCurrentWayPoints = {0,0,0,0,0};
	public static int[] squadOneReportingChannels = {1,2,3,4,5};
	public static int[] squadOneReceivingChannels = {6,7,8,9,10};
	public static ArrayList<Short> toSpawn = new ArrayList<Short>();
	public static int lastRobotCount = 0;
	public static MapLocation currentOffensiveTarget;
	public static int OffenseChannel = 250;
	public static void runHeadQuarters(RobotController rcIn) {
		rc = rcIn;
		currentOffensiveTarget = rc.getLocation();
		//Set up the list of things to Spawn
		toSpawn.add((short) 0);
		toSpawn.add((short) 9);
		for(short i=1; i<9; i+=2){
			toSpawn.add(i);
			toSpawn.add((short)500);
		}
		try{//initialize the internal map
			int cellSize = 3; //start with 2 and increase if the map is large
			if(rc.getMapHeight()*rc.getMapWidth()> 2000)
				cellSize+=2;
			if(rc.getMapHeight()*rc.getMapWidth()> 4000)
				cellSize+=2;
			if(rc.getMapHeight()*rc.getMapWidth()> 6000)
				cellSize+=2;
			if(rc.getMapHeight()*rc.getMapWidth()> 8000)
				cellSize+=2;
			if(rc.getMapHeight()*rc.getMapWidth()> 10000)
				cellSize+=2;
			while(!AStarPathing.InitailizeIteratorInator(rc, cellSize)){ //while our map isn't done yet, spawn as many robots as possible
				MakeABaby();
			}
			System.out.println("Finished Making Map");
		}catch (Exception e){
			e.printStackTrace();
		}
		//Figure out where all the cows are
		int bestscore = 0;
		MapLocation bestLoc = new MapLocation(0,0);
		double[][] cows = rc.senseCowGrowth();
		MapLocation myLoc = rc.getLocation();
		MapLocation theirLoc = rc.senseEnemyHQLocation();
		for(int i =0; i<cows.length;i++){
			for(int j=0; j<cows[0].length;j++){
				if(theirLoc.distanceSquaredTo(new MapLocation(i,j))!=0 &&myLoc.distanceSquaredTo(new MapLocation(i,j))!=0){
					int score = (int) (cows[i][j]*50 + Math.pow(theirLoc.distanceSquaredTo(new MapLocation(i,j))/myLoc.distanceSquaredTo(new MapLocation(i,j)), 0.5));
					if(score>bestscore && checkAround(i,j)){
						bestscore = score;
						bestLoc = new MapLocation(i,j);
					}
				}
			}
		}
		//bestLoc = rc.getLocation().add(rc.getLocation().directionTo(rc.senseEnemyHQLocation()).rotateLeft().rotateLeft().rotateLeft().rotateLeft());
		while(true)
			try {
				rc.broadcast(999, (bestLoc.x*100)+bestLoc.y);
				break;
			} catch (GameActionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		//First part of our plan, spawn soldiers while waiting for path to inf milk
		System.out.println("Best Loc x"+Integer.toString(bestLoc.x)+"Best Loc y"+Integer.toString(bestLoc.y)); //DEBUG
		ArrayList<MapLocation> PathToFollow = AStarPathing.returnBestPathIteratorInator(rc.getLocation(), bestLoc);
		while(PathToFollow==null){//while we are waiting for a path, spawn soldiers
			try {
				shootClosestEnemy();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MakeABaby();
			PathToFollow = AStarPathing.returnBestPathIteratorInator(rc.getLocation(), bestLoc);
		}
		if(PathToFollow.get(0).x == -1){
			//Something broke. SHIT!
			//Add a waypoint at the end of the path, and hope the soldiers figure it out
			PathToFollow = new ArrayList<MapLocation>();
			PathToFollow.add(bestLoc);
			System.out.println("Gonna BS this");
		}
		System.out.println("Pathing Finished"); //DEBUG
		//Send out the pathing info
		while(true){
			try {
				for(int i=PathToFollow.size()-1; i>=0; i--){
					rc.broadcast(i+50, 10000+(PathToFollow.get(i).x*100)+PathToFollow.get(i).y);
				}
				System.out.println("Wrote"+Integer.toString(100000+((int)PathToFollow.get(0).x*100)+(int)PathToFollow.get(0).y));
				break;
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Pathing Sent"); //DEBUG
		for(int i = 0; i<100; i++){
			try {
				int input = rc.readBroadcast(666);
				if(input>4){
					rc.broadcast(666, 0);
					break;
				}
				handleRespawning();
				MakeABaby();
				rc.broadcast(666, 0);
				rc.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while(true){//Now Make our Offensive Team Kill!!!!
			MapLocation[] locs = rc.sensePastrLocations(rc.getTeam().opponent());
			ArrayList<MapLocation> PathToFollowOff;
			//Debug
			for(MapLocation location:locs){
				System.out.println(Integer.toString(location.x)+" "+Integer.toString(location.y));
			}
			if(locs.length>0)//if the enemy has a pastr, destroy the bloody thing!
			{
				MapLocation BestPastr = locs[0];
				int bestScore = 0;
				for(MapLocation loc: locs){
					int score = rc.senseEnemyHQLocation().distanceSquaredTo(loc);
					if(score>bestScore){
						BestPastr = loc;
						bestScore = score;
					}
				}
				
				PathToFollowOff = AStarPathing.returnBestPathIteratorInator(rc.getLocation(), BestPastr);
				while(PathToFollowOff==null){//while we are waiting for a path, spawn soldiers
					try {
						shootClosestEnemy();
					} catch (GameActionException e) {
						e.printStackTrace();
					}
					handleRespawning();
					MakeABaby();
					PathToFollowOff = AStarPathing.returnBestPathIteratorInator(rc.getLocation(), BestPastr);
				}
				PathToFollowOff.add(BestPastr);
			}else{
				//If the enemy hasn't built anything, their probably destroying our pastr. Thats not good.
				PathToFollowOff = AStarPathing.returnBestPathIteratorInator(rc.getLocation(), bestLoc);//Path to our pastr
				while(PathToFollowOff==null){//while we are waiting for a path, spawn soldiers
					try {
						shootClosestEnemy();
					} catch (GameActionException e) {
						e.printStackTrace();
					}
					handleRespawning();
					MakeABaby();
					PathToFollowOff = AStarPathing.returnBestPathIteratorInator(rc.getLocation(), bestLoc);
				}
				PathToFollowOff.add(bestLoc);
			}
			
			//Okay, we have our target, now lets go kill them.
			//First things first, let our champions know where they need to go to KILL!
			while(true){
				try {
					for(int i=PathToFollowOff.size()-1; i>=1; i--){
						rc.broadcast(i+250, 10000+(PathToFollowOff.get(i).x*100)+PathToFollowOff.get(i).y);
					}
					break;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			//Make sure to add a stop message
			while(true){
				try {
					rc.broadcast(250+PathToFollowOff.size(), 0);
					break;
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Okay, now we wait until we have critical mass, then we attack!
			while(true){
				try {
					int input = rc.readBroadcast(666);
					if(input>8){
						rc.broadcast(666, 0);
						break;
					}
					handleRespawning();
					MakeABaby();
					rc.broadcast(666, 0);
					rc.yield();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Time ta kill");
			//Send the final message...
			while(true){
				try {
					rc.broadcast(250, 10000+(PathToFollowOff.get(0).x*100)+PathToFollowOff.get(0).y);
					break;
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Wait three turns...
			rc.yield();
			rc.yield();
			rc.yield();
			//And delete the final message
			while(true){
				try {
					rc.broadcast(250, 0);
					break;
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			int currTime = Clock.getRoundNum();
			//Now make more soldiers, so that we are ready for the next wave, and call retreat when necessary
			while(true){
				if(Clock.getRoundNum()-currTime>250){
					//Time to retreat, and prep the next wave
					while(true){
						try {
							rc.broadcast(1000, 4);
							rc.yield();rc.yield();rc.yield();//wait three turns, before deleting retreat signal
							rc.broadcast(1000, 0);
							break;
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				}
				handleRespawning();
				MakeABaby();
			}
		}
	}
	public static void handleRespawning(){
		//If we be missin bots, make sum more
		if(rc.senseRobotCount()< lastRobotCount){
			respawn();
		}
		lastRobotCount = rc.senseRobotCount();
		
	}
	public static void respawn(){
		System.out.println("Uh Oh");
		try {
			rc.yield(); //wait a turn
			for(int i=1;i<9;i+=2){
				if(rc.readBroadcast(i+1) != 1 &&!toSpawn.contains((short)i+1)){
					toSpawn.add(0, (short) i);

					System.out.println("respawn noisetower");
				}
			}
			if(rc.readBroadcast(9) != 1 &&!toSpawn.contains(9)){
				toSpawn.add(0, (short) 9);

				System.out.println("respawn noisetower");
			}
			rc.broadcast(9,0);
			if(rc.readBroadcast(1) != 1 &&!toSpawn.contains(0)){
				toSpawn.add(0, (short) 0);

				System.out.println("respawn PASTR");
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static boolean MakeABaby(){
		//Attack if Possible
		try {
			shootClosestEnemy();
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(rc.isActive()&&rc.senseRobotCount()< GameConstants.MAX_ROBOTS&&toSpawn.size()>0){
			try{
				for(int i = 0; i<8; i++){
					if(rc.canMove(allSpawningDirections[i])){
						rc.spawn(allSpawningDirections[i]);
						rc.broadcast(0,toSpawn.get(0));
						toSpawn.remove(0);
						return true;
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}else{
			try{
				for(int i = 0; i<8; i++){
					if(rc.isActive()&&rc.senseRobotCount()< GameConstants.MAX_ROBOTS&&rc.canMove(allSpawningDirections[i])){
						rc.spawn(allSpawningDirections[i]);
						rc.broadcast(0,500);
						return true;
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}
	public static boolean checkAround(int x,int y){
		boolean allClear = true;
		int[][] adjacent = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1},{0,2}};
		for(int[] i:adjacent){
			if(rc.senseTerrainTile(new MapLocation(x+i[0],y+i[1])) == TerrainTile.VOID || rc.senseTerrainTile(new MapLocation(x+i[0],y+i[1])) == TerrainTile.OFF_MAP ){
				allClear = false;
			}
		}
		return allClear;
	}
	public static void shootClosestEnemy() throws GameActionException{
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
		if(enemyRobots.length>0){
			RobotInfo closest = rc.senseRobotInfo(enemyRobots[0]);
			int disToBeat=100000000;
			for(Robot enemyRobot:enemyRobots){
				if(rc.getLocation().distanceSquaredTo(rc.senseRobotInfo(enemyRobot).location)<disToBeat){
					disToBeat = rc.getLocation().distanceSquaredTo(rc.senseRobotInfo(enemyRobot).location);
					closest = rc.senseRobotInfo(enemyRobot);
				}
			}
			if(closest.location.distanceSquaredTo(rc.getLocation()) < rc.getType().attackRadiusMaxSquared &&closest.type !=battlecode.common.RobotType.HQ){
				if(rc.isActive()){
					rc.attackSquare(closest.location);
				}
			}
		}
	}
}