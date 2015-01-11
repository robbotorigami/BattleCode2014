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
	//shoot enemy with lowest HP within range
	public void shootWeakest() throws GameActionException{
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		double LowestHealth = 0;
		RobotInfo weakestLink = null;
		for(RobotInfo ri:enemiesInRange){
			if(ri.health>LowestHealth){
				weakestLink = ri;
				LowestHealth = ri.health;
			}
		}
		if(weakestLink != null){
			if(rc.isWeaponReady()&&rc.canAttackLocation(weakestLink.location)){
				rc.attackLocation(weakestLink.location);
			}
		}
	}
	public void launchAtWeakest() throws GameActionException{
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		double LowestHealth = 10000;
		RobotInfo weakestLink = null;
		for(RobotInfo ri:enemiesInRange){
			if(ri.type == RobotType.TOWER){
				weakestLink = ri;
				break;
			}
			if(ri.health<LowestHealth){
				weakestLink = ri;
				LowestHealth = ri.health;
			}
			
		}
		
		if(weakestLink != null){
			if(rc.isWeaponReady()&&rc.canLaunch(rc.getLocation().directionTo(weakestLink.location))&&rc.getMissileCount()>=1){
				rc.launchMissile(rc.getLocation().directionTo(weakestLink.location));
			}
		}
	}
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
	
	//Returns the index value of the provided direction
	public int getIndexOfDirection(Direction dir){
		Direction[] values = Direction.values();
		for(int i =0; i<8; i++){
			if(values[i]==dir){
				return i;
			}
		}
		//If no match is found, return north
		return 0;
	}
	
	//Calculates all of the ore that is withing one square of the robot
	public int getOreNear(){
		MapLocation[] near = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 2);
		double oreSum = 0;
		for(MapLocation loc: near){
			oreSum += rc.senseOre(loc);
		}
		return (int) oreSum;
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
	
	//Returns an array of RobotInfo of all of the robots that are of a specific type on team
	public RobotInfo[] robotsOnTeam(RobotType type, Team team){
		RobotInfo[] Robots = rc.senseNearbyRobots(10000000, team);
		RobotInfo[] ofType = {};
		int index = 0;
		for(RobotInfo ri:Robots){
			if(ri.type == type){
				ofType[index] = ri;
				index++;
			}
		}
		return ofType;
	}
}
