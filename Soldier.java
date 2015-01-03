package team210;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Soldier{
	public static RobotController rc;
	public static int sqaud;
	public static int reportingNumber;
	static ArrayList<MapLocation> oldLocs = new ArrayList<MapLocation>();
	static int[] DirectionalPreferences = {0,-1,1,-2,2,-3,3,4};
	static int wayPointChannel = 250;
	static MapLocation target = new MapLocation(0,0);
	static int RoleInLife = 0;
	static Random rand = new Random();
	public static int lastRobotCount = 0;
	public static void run(RobotController rcIn){
		//We've been created, so read our assigned id stuffs
		rc = rcIn;
		rand.setSeed(rc.getRobot().getID());
		try {
			if(rc.readBroadcast(0)<10){
				RoleInLife = rc.readBroadcast(0);
				wayPointChannel = 50;
			}else if(rc.readBroadcast(0)==500){
				RoleInLife = rc.readBroadcast(0);
				wayPointChannel = 250;
			}else{
				RoleInLife = 500;
				wayPointChannel = 250;
			}
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		rc.setIndicatorString(1, "wayPointChannel"+Integer.toString(wayPointChannel));
		
		if(RoleInLife == 500){//Run Offense
			ArrayList<MapLocation> PathToKill = new ArrayList<MapLocation>();
			rc.setIndicatorString(0, "#OnTheOffense");
			int input2;
			while(true)
			try {
				input2 = rc.readBroadcast(wayPointChannel);
				break;
			} catch (GameActionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				
			MapLocation CoolTable = rc.senseHQLocation().add(rc.senseHQLocation().directionTo(new MapLocation(input2/100,input2%100)).opposite(), 1);
			while(true){
				while(true){
					int input;
					try {//let the hq know we are ready
						rc.broadcast(666,rc.readBroadcast(666)+1);				
						
						input = rc.readBroadcast(wayPointChannel);

						if(input/10000==1){//If there is a new waypoint for us to go to, stop waiting, and go!
							break;
						}
						//Attack if Possible
						shootClosestEnemy();
						//Otherwise, keep trying to hang out with the cool kids (Unless we don't need to)
						if(rc.getLocation().distanceSquaredTo(CoolTable)>4){
							SimpleMove(CoolTable, false);
						}
						
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rc.yield();
				}
				PathToKill = new ArrayList<MapLocation>();
				//Lets Read our path to follow!
				while(true){
					try{
						int input;
						input = rc.readBroadcast(wayPointChannel);
						if(input/10000==1){//If we have not hit the end of the message, add it to the path and keep going
							input = input%10000;
							PathToKill.add(new MapLocation(input/100,input%100));
							wayPointChannel++;
						}else{
							break;
						}
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				wayPointChannel = 250;
				rc.setIndicatorString(1, Integer.toString(PathToKill.size()));
				//Alright! now lets move along that path, killing if we can
				int waypoint = 0;
				while(true){
					try{
						rc.setIndicatorString(2, Integer.toString(waypoint));
						//Attack if Possible
						shootClosestEnemy();
						//Try to move to waypoint
						SimpleMove(PathToKill.get(waypoint), false);
						//If close, then move to next
						if(rc.getLocation().distanceSquaredTo(PathToKill.get(waypoint))<=4){
							waypoint++;
						}
						if(waypoint == PathToKill.size()-1){
							break;
						}
						rc.yield();
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//Now we just Kill, until we recieve retreat order
				while(true){
					try{
						//Attack if Possible
						shootClosestEnemy();
						//Try to move to waypoint
						SimpleMove(PathToKill.get(0), false);
						if(rc.readBroadcast(1000)==4){
							break;
						}
						rc.yield();
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				waypoint = PathToKill.size()-1;
				//Now Move Back, while still killing
				while(true){
					try{
						//Attack if Possible
						shootClosestEnemy();
						//Try to move to waypoint
						SimpleMove(PathToKill.get(waypoint), false);
						//If close, then move to next
						if(rc.getLocation().distanceSquaredTo(PathToKill.get(waypoint))<=4){
							waypoint--;
						}
						if(waypoint == 0){
							break;
						}
						rc.yield();
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		while(true){
			int input;
			try {
				input = rc.readBroadcast(wayPointChannel);
				rc.setIndicatorString(0, "Reading:"+Integer.toString(input));
				if(input/10000==1){
					break;
				}
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while(true){
			//goto pastr lcoation
			try {
				int input= rc.readBroadcast(wayPointChannel);
				if(input/10000==1){
					input = input%10000;
					target = new MapLocation(input/100,input%100);
				}else{
					break;
				}
				//Try to hit any enemys in range
				shootClosestEnemy();
				//Try to move to waypoint
				SimpleMove(target, false);
				if(rc.getLocation().distanceSquaredTo(target)<=4){
					wayPointChannel++;
				}
				//If someone died, let them know it wasn't us
				if(rc.senseRobotCount()< lastRobotCount){
					try {
						rc.broadcast(RoleInLife,1);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				lastRobotCount = rc.senseRobotCount();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			rc.yield();
		}
		if(RoleInLife>0 && RoleInLife <9){
			Direction addDir = Direction.SOUTH;
			for(int i = 0; i<RoleInLife; i++){
				addDir = addDir.rotateLeft();
				rc.setIndicatorString(2, Integer.toString(i));
			}
			target = target.add(addDir);
		}
		if(RoleInLife==9){
			target = target.add(Direction.NORTH,2);
		}
		rc.setIndicatorString(1, "Target" +Integer.toString(target.x)+","+Integer.toString(target.y));
		while(rc.getLocation().distanceSquaredTo(target)>1.1){
			try {
				SimpleMove(target, true);
				rc.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i =0; i<4; i++){
			try {
				if(rc.getLocation().distanceSquaredTo(target)>0)
					SimpleMove(target, true);
				rc.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(RoleInLife == 0){
			for(int i = 0; i<80;i++){
				rc.yield();
			}
			while(true){
				if(rc.isActive())
					try {
						//System.out.println("PASTR?");//DEBUG
						rc.construct(RobotType.PASTR);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		if(RoleInLife == 9){
			while(true){
				if(rc.isActive())
					try {
						rc.construct(RobotType.NOISETOWER);
						target = target.add(Direction.SOUTH, 2);
						rc.broadcast(303, target.x*100+target.y+10000);
						rc.yield();
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		if(RoleInLife>0 && RoleInLife <9){
			while(true){
				try {
					shootClosestEnemy();
					rc.yield();
					if(rc.senseRobotCount()< lastRobotCount){
						try {
							rc.broadcast(RoleInLife+1,1);
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					lastRobotCount = rc.senseRobotCount();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		while(true){}/*else{
			while(true){
				MapLocation randomLoc = new MapLocation((int) (rc.getLocation().x+(rand.nextDouble()*20-10)),(int) (rc.getLocation().y+(rand.nextDouble()*20-10)));

				for(int i = 0; i<10; i++){
					while(!rc.isActive()){rc.yield();}
					try {
						shootClosestEnemy();
						SimpleMove(randomLoc, true);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for(int i = 0; i<12; i++){
					while(!rc.isActive()){rc.yield();}
					try {
						shootClosestEnemy();
						SimpleMove(target, false);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}*/
	}
	private static void SimpleMove(MapLocation target, boolean sneak) throws GameActionException {
		while(oldLocs.size()<2)
			oldLocs.add(new MapLocation(-1,-1));
		if(rc.isActive()&&rc.getLocation().distanceSquaredTo(target)!=0){
			oldLocs.remove(0);
			oldLocs.add(rc.getLocation());
			Direction targetDirection = rc.getLocation().directionTo(target);
			for(int directionalOffset: DirectionalPreferences){
				int forward = targetDirection.ordinal();
				Direction trialDir = Direction.values()[(forward+directionalOffset+8)%8];
				if(rc.getLocation().distanceSquaredTo(target)<16){
					if(canMove(trialDir)){
						if(sneak){
							rc.sneak(trialDir);
						}else{
							rc.move(trialDir);
						}
						break;
					}
				}else{
					if(canMove(trialDir)){
						if(sneak){
							rc.sneak(trialDir);
						}else{
							rc.move(trialDir);
						}
						break;
					}
				}
			}
		}
		
	}
	public static boolean canMove(Direction tryDir){
		MapLocation resultingLocation = rc.getLocation().add(tryDir);
		for(int i=0;i<oldLocs.size();i++){
			MapLocation m = oldLocs.get(i);
			if(!m.equals(rc.getLocation())){
				if(resultingLocation.isAdjacentTo(m)||resultingLocation.equals(m)){
					return false;
				}
			}
		}
		return rc.canMove(tryDir);
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