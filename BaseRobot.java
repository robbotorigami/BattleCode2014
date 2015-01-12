package team079;

import java.util.ArrayList;
import java.util.Random;

import team079.util.BetterMapLocation;
import team079.util.ComSystem;
import battlecode.common.*;

public abstract class BaseRobot {
	public RobotController rc;
	public Random rand; //Random number generator
	public MapLocation theirHQ; //location of their HQ
	public MapLocation ourHQ; //location of our HQ
	public ArrayList oldLocs;
	public Direction lastDir;
	public Direction lastTried;
	public boolean useBug;
	public final int NUMOLDLOCS = 10;
	
	public BaseRobot(RobotController rcin){
		rc = rcin;
		rand = new Random(rc.getID());
		ComSystem.init(rc);
		ourHQ = rc.senseHQLocation();
		theirHQ = rc.senseEnemyHQLocation();
		BetterMapLocation.init(rc);
		
		oldLocs = new ArrayList();
		for(int i =0; i<NUMOLDLOCS; i++){
			oldLocs.add(new MapLocation(0,0));
		}
		lastDir = Direction.NORTH;
		useBug = false;
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
			if(ri.type == RobotType.HQ || ri.type == RobotType.TOWER){
				weakestLink = ri;
				break;
			}
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
					break;
				}
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
					break;
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
		RobotInfo[] ofType = new RobotInfo[100];
		int index = 0;
		for(RobotInfo ri:Robots){
			if(ri.type == type){
				ofType[index] = ri;
				index++;
			}
		}
		
		return ofType;
	}
	
	public boolean basicPathing(Direction toMove) throws GameActionException{
		if(rc.isCoreReady()){
			/*if(useBug){
				return bugPath(toMove);
			}*/
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
				boolean badLoc = false;
				if(oldLocs.contains(rc.getLocation().add(dir))){
					badLoc = true;
				}
				if(rc.canMove(dir)&&rc.isCoreReady() && !badLoc){
					if(dir != toMove){
						useBug = true;
					}
					oldLocs.add(rc.getLocation().add(dir));
					oldLocs.remove(0);
					lastDir = dir;
					lastTried = toMove;
					rc.move(dir);
					return true;
				}
			}
		}	
		return false;
	}
	
	public boolean basicPathingSafe(Direction toMove) throws GameActionException{
		if(rc.isCoreReady()){
			/*if(useBug){
				return bugPath(toMove);
			}*/
			Direction[] toTry = {toMove,
					toMove.rotateLeft(),
					toMove.rotateRight(),
					toMove.rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight(),
					toMove.rotateLeft().rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight().rotateRight(),
					toMove.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
			};
			RobotInfo[] Robots = rc.senseNearbyRobots(40, rc.getTeam().opponent());
			for(Direction dir:toTry){
				boolean badLoc = false;
				if(oldLocs.contains(rc.getLocation().add(dir))){
					badLoc = true;
				}
				for(RobotInfo ri: Robots){
					if(rc.getLocation().add(dir).distanceSquaredTo(ri.location) <= ri.type.attackRadiusSquared){
						badLoc = true;
					}
				}
				for(MapLocation loc: rc.senseEnemyTowerLocations()){
					if(rc.getLocation().add(dir).distanceSquaredTo(loc) <=RobotType.TOWER.attackRadiusSquared){
						badLoc = true;
					}
				}
				
				if(rc.canMove(dir)&&rc.isCoreReady() && !badLoc){
					if(dir != toMove){
						useBug = true;
					}
					oldLocs.add(rc.getLocation().add(dir));
					oldLocs.remove(0);
					lastDir = dir;
					lastTried = toMove;
					rc.move(dir);
					return true;
				}
			}
		}	
		return false;
	}
	
	public boolean bugPath(Direction toMove) throws GameActionException{
		boolean turnClockwise = true;
		for(int i = 1; i<=3; i++){
			Direction test = lastTried;
			for(int j = 0; j<i; j++);
				test = test.rotateLeft();
			if(test == lastDir){
				turnClockwise = false;
			}
		}
		rc.setIndicatorString(1, "Clockwise: "+turnClockwise);
		Direction[] toTry;
		if(turnClockwise){
			Direction[] toTry2 = {
					toMove,
					toMove.rotateRight(),
					toMove.rotateRight().rotateRight(),
					toMove.rotateRight().rotateRight().rotateRight(),
					toMove.rotateRight().rotateRight().rotateRight().rotateRight(),
					toMove.rotateRight().rotateRight().rotateRight().rotateRight().rotateRight(),
					toMove.rotateRight().rotateRight().rotateRight().rotateRight().rotateRight().rotateRight(),
					toMove.rotateRight().rotateRight().rotateRight().rotateRight().rotateRight().rotateRight().rotateRight()
			};
			toTry = toTry2;
		}else{
			Direction[] toTry2 = {
					toMove,
					toMove.rotateLeft(),
					toMove.rotateLeft().rotateLeft(),
					toMove.rotateLeft().rotateLeft().rotateLeft(),
					toMove.rotateLeft().rotateLeft().rotateLeft().rotateLeft(),
					toMove.rotateLeft().rotateLeft().rotateLeft().rotateLeft().rotateLeft(),
					toMove.rotateLeft().rotateLeft().rotateLeft().rotateLeft().rotateLeft().rotateLeft(),
					toMove.rotateLeft().rotateLeft().rotateLeft().rotateLeft().rotateLeft().rotateLeft().rotateLeft()
			};
			toTry = toTry2;
		}
		for(Direction dir:toTry){
			boolean badLoc = false;
			if(oldLocs.contains(rc.getLocation().add(dir))){
				badLoc = true;
			}
			if(rc.canMove(dir)&&rc.isCoreReady() && !badLoc){
				if(dir == toMove){
					useBug = false;
				}
				oldLocs.add(rc.getLocation().add(dir));
				oldLocs.remove(0);
				rc.move(dir);
				return true;
			}
		}
		return false;
	}
	
	public boolean clearPath(MapLocation A, MapLocation B) throws GameActionException{
		MapLocation toTest = A.add(A.directionTo(B));
		while(!toTest.equals(B)){
			if(rc.senseRobotAtLocation(toTest)!= null){
				return false;
			}
			toTest = toTest.add(toTest.directionTo(B));
		}
		return true;
	}
	
	public void logAllNearbyOffMap(MapLocation loc) throws GameActionException{
		MapLocation[] near = MapLocation.getAllMapLocationsWithinRadiusSq(loc, 2);
		for(MapLocation square: near){
			if(rc.senseTerrainTile(square) == TerrainTile.OFF_MAP){
				ComSystem.logOffMap(square);
			}
		}
	}
}
