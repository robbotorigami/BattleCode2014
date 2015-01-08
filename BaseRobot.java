package team079;

import java.util.Random;

import team079.util.BetterMapLocation;
import team079.util.ComSystem;
import battlecode.common.*;

public abstract class BaseRobot {
	public RobotController rc;
	public Random rand; //Random number generator
	public MapLocation theirHQ; //location of their HQ
	public MapLocation ourHQ; //location of our HQ
	
	public BaseRobot(RobotController rcin){
		rc = rcin;
		rand = new Random(rc.getID());
		ComSystem.init(rc);
		ourHQ = rc.senseHQLocation();
		theirHQ = rc.senseEnemyHQLocation();
		BetterMapLocation.init(rc);
	}
	
	//Abstract method for major functionality
	public abstract void run() throws GameActionException;
	
	//-------------------Base Methods for all Units------------------
	
	//Will try to spawn a Unit of a given type
	public void spawnUnit(RobotType toSpawn) throws GameActionException{
		if(rc.isCoreReady()){
			boolean unitSpawned = false;
			while(unitSpawned != true){
				//Have a list of all directions biased towards their HQ. try and spawn for all in list
				Direction dir = ourHQ.directionTo(theirHQ);
				Direction[] toTry = {dir,
						dir.rotateLeft(),
						dir.rotateRight(),
						dir.rotateLeft().rotateLeft(),
						dir.rotateRight().rotateRight(),
						dir.rotateLeft().rotateLeft().rotateLeft(),
						dir.rotateRight().rotateRight().rotateRight(),
						dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
				};
				for(Direction spawnDir: toTry){
					if(rc.isCoreReady()&&rc.canSpawn(spawnDir, toSpawn)){					
						rc.spawn(spawnDir, toSpawn);
						unitSpawned = true;
						//System.out.println("Made a " + toSpawn);
						break;
					}
				}
				rc.yield();
			}
		}

	}
	
	//Builds a unit in a random direction
	public void buildUnit(RobotType toBuild) throws GameActionException{
		if(rc.isCoreReady()){
			boolean unitBuilt = false;
			while(unitBuilt != true){
				Direction dir = getRandomDirection();
				Direction[] toTry = {dir,
						dir.rotateLeft(),
						dir.rotateRight(),
						dir.rotateLeft().rotateLeft(),
						dir.rotateRight().rotateRight(),
						dir.rotateLeft().rotateLeft().rotateLeft(),
						dir.rotateRight().rotateRight().rotateRight(),
						dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
				};
				for(Direction buildDir: toTry){
					if(rc.isCoreReady()&&rc.canBuild(buildDir, toBuild)){			
						rc.build(buildDir, toBuild);
						unitBuilt = true;
						break;
					}
				}
				rc.yield();
			}
		}
	}
	
	//Move as close as possible to the provided direction
	public void moveAsCloseToDirection(Direction toMove) throws GameActionException{
		if(rc.isCoreReady()){
			Direction[] toTry = {toMove,
					toMove.rotateLeft(),
					toMove.rotateRight(),
					toMove.rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight(),
					toMove.rotateLeft().rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight().rotateRight(),
					toMove.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
			};
			for(Direction dir:toTry){
				if(rc.canMove(dir)&&rc.isCoreReady()){
					rc.move(dir);
				}
			}
		}		
	}
	
	//Returns a random direction	
	public Direction getRandomDirection(){
		return Direction.values()[rand.nextInt(8)];
	}
	
	//Returns the number of robots that can be sensed of type type on the team provided
	public int robotsOfTypeOnTeam(RobotType type, Team team) {
		RobotInfo[] Robots = rc.senseNearbyRobots(10000000, team);
		int number = 0;
		for(RobotInfo ri: Robots){
			if(ri.type==type){
				number++;
			}
		}
		return number;
	}
}
