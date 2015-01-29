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
	public ArrayList<MapLocation> oldLocs;
	public Direction lastDir;
	public Direction lastTried;
	public boolean useBug;
	public final int NUMOLDLOCS = 10;
	public BugInfo bugPathing;
	
	public class BugInfo{
		public boolean wallFollow;
		public MapLocation from;
		public int dmin;
		public int dleave;
		
	}
	
	public BaseRobot(RobotController rcin){
		rc = rcin;
		rand = new Random(rc.getID());
		ourHQ = rc.senseHQLocation();
		theirHQ = rc.senseEnemyHQLocation();
		ComSystem.init(rc);
		BetterMapLocation.init(rc);

		oldLocs = new ArrayList<MapLocation>();
		for(int i =0; i<NUMOLDLOCS; i++){
			oldLocs.add(new MapLocation(0,0));
		}
		lastDir = Direction.NORTH;
		useBug = false;
		bugPathing = new BugInfo();
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
	public boolean buildUnit(RobotType toBuild) throws GameActionException{
		if(toBuild == null) return false;
		if(rc.isCoreReady()){
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
				BetterMapLocation wouldBeBuilt = new BetterMapLocation(rc.getLocation().add(buildDir));
				if(wouldBeBuilt.x%2 == wouldBeBuilt.y%2){
					if(rc.isCoreReady()&&rc.canBuild(buildDir, toBuild)){	
						rc.build(buildDir, toBuild);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	//Move as close as possible to the provided direction
	public boolean moveAsCloseToDirection(Direction toMove) throws GameActionException{
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
					return true;
				}
			}
		}	
		return false;
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
		RobotInfo[] ofType = new RobotInfo[1000];
		int index = 0;
		for(RobotInfo ri:Robots){
			if(ri.type == type){
				ofType[index] = ri;
				index++;
			}
		}
		RobotInfo[] fixyfix = new RobotInfo[index];
		for(int i = 0; i<index; i++){
			fixyfix[i] = ofType[i];
		}
		
		return fixyfix;
	}
	
	public RobotInfo[] robotsOnTeam(RobotType type, int distance, Team team){
		RobotInfo[] Robots = rc.senseNearbyRobots(distance, team);
		RobotInfo[] ofType = new RobotInfo[1000];
		int index = 0;
		for(RobotInfo ri:Robots){
			if(ri.type == type){
				ofType[index] = ri;
				index++;
			}
		}
		RobotInfo[] fixyfix = new RobotInfo[index];
		for(int i = 0; i<index; i++){
			fixyfix[i] = ofType[i];
		}
		
		return fixyfix;
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
	
	public boolean basicPathingDiag(Direction toMove) throws GameActionException{
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
				if(rc.getLocation().add(dir).x%2 == rc.getLocation().add(dir).y%2){
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
				
				if(rc.getLocation().add(dir).distanceSquaredTo(rc.senseEnemyHQLocation()) <=RobotType.HQ.attackRadiusSquared){
					badLoc = true;
				}
				
				if(rc.canMove(dir)&&rc.isCoreReady() && !badLoc){
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
	
	public void initBetterPathing(MapLocation from){
		bugPathing.from = from;
	}
	
	public boolean betterPathing(MapLocation toPathTo) throws GameActionException{
		rc.setIndicatorString(0, "");
		rc.setIndicatorString(1, "");
		Direction dir = rc.getLocation().directionTo(toPathTo);
		MapLocation check = rc.getLocation().add(dir);
		boolean allClear = true;
		//Check if there are obsticles in our sensing range
		while(rc.getLocation().distanceSquaredTo(check) < rc.getType().sensorRadiusSquared){
			if(rc.senseTerrainTile(check) != TerrainTile.NORMAL || rc.senseRobotAtLocation(check) != null){
				allClear = false;
				break;
			}
			check = check.add(dir);
		}
		if(allClear){
			//If there are no obsticles, move in the direction if possible
			if(rc.canMove(dir) && rc.isCoreReady()){
				rc.move(dir);
				return true;
			}else{
				return false;
			}
		}else{
			//If there is an obsticle, find the leftmost and rightmost points of it
			//Note that we want to find the ones closest to us
			Direction[] toCheckLeft = {dir.opposite(),
					dir.rotateLeft().rotateLeft().rotateLeft(),
					dir.rotateLeft().rotateLeft(),
					dir.rotateLeft()};
			Direction[] toCheckRight = {dir.opposite(),
					dir.rotateRight().rotateRight().rotateRight(),
					dir.rotateRight().rotateRight(),
					dir.rotateRight()};
			MapLocation leftMost = check;
			MapLocation rightMost = check;
			
			//While we can sense the squares, try and find a blocked tile that is more to the left
			outer: while(rc.canSenseLocation(leftMost)){
				MapLocation old = leftMost;
				for(Direction toCheck: toCheckLeft){
					MapLocation locToCheck = leftMost.add(toCheck);
					if(rc.canSenseLocation(locToCheck)){
						if(rc.senseTerrainTile(locToCheck) != TerrainTile.NORMAL){// || rc.senseRobotAtLocation(locToCheck)!= null){
							leftMost = locToCheck;
							if(rc.getID() == 7727){
								System.out.println(locToCheck + ", " + rc.senseTerrainTile(locToCheck));
							}
							break;
						}
					}else{
						break outer;
					}
				}
				if(old.equals(leftMost)){
					break;
				}
			}
			
			//While we can sense the squares, try and find a blocked tile that is more to the right
			outer: while(rc.canSenseLocation(rightMost)){
				MapLocation old = rightMost;
				for(Direction toCheck: toCheckRight){
					MapLocation locToCheck = rightMost.add(toCheck);
					if(rc.canSenseLocation(locToCheck)){
						if(rc.senseTerrainTile(locToCheck) != TerrainTile.NORMAL){// || rc.senseRobotAtLocation(locToCheck)!= null){
							rightMost = locToCheck;
							break;
						}
					}else{
						break outer;
					}
				}
				if(old.equals(rightMost)){
					break;
				}
			}
			rc.setIndicatorString(0, leftMost.toString());
			rc.setIndicatorString(1, rightMost.toString());
			rc.setIndicatorString(2, check.toString());
			//if(rc.getID()== 206) printPathingVis(leftMost, rightMost);
			//If our qstart loc isnt defined, define dat shit
			if(bugPathing.from ==null) bugPathing.from = rc.senseHQLocation();

			//If the heurestric for left better then the right then it win else right win
			int leftHurestic = bugPathing.from.distanceSquaredTo(leftMost)+toPathTo.distanceSquaredTo(leftMost);
			int rightHurestic = bugPathing.from.distanceSquaredTo(rightMost)+toPathTo.distanceSquaredTo(rightMost);
			if(leftHurestic < rightHurestic){
				//If there are no obsticles, move in the direction if possible
				dir = rc.getLocation().directionTo(leftMost);
				return moveAsCloseToDirection(dir);
			}else if(rightHurestic < leftHurestic){
				//If there are no obsticles, move in the direction if possible
				dir = rc.getLocation().directionTo(rightMost);
				return moveAsCloseToDirection(dir);
			}else{
				if(rand.nextDouble() <0.5){
					//If there are no obsticles, move in the direction if possible
					dir = rc.getLocation().directionTo(leftMost);
					return moveAsCloseToDirection(dir);
				}else{
					//If there are no obsticles, move in the direction if possible
					dir = rc.getLocation().directionTo(rightMost);
					return moveAsCloseToDirection(dir);
				}
			}
		}
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
	public void printPathingVis(MapLocation leftMost, MapLocation rightMost) throws GameActionException{
		MapLocation[] vis = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), rc.getType().sensorRadiusSquared);
		for(int j = rc.getLocation().y-15; j< rc.getLocation().y+15; j++){
			for(int i = rc.getLocation().x-15; i< rc.getLocation().x+15; i++){
				MapLocation place = new MapLocation(i,j);
				if(rc.getLocation().distanceSquaredTo(place) < rc.getType().sensorRadiusSquared){
					if(place.equals(leftMost)||place.equals(rightMost)){
						System.out.print("Y");
						continue;
					}
					if(place.equals(rc.getLocation())){
						System.out.print("R");
						continue;
					}
					if(rc.senseRobotAtLocation(place)!=null){
						System.out.print("X");
					}
					switch(rc.senseTerrainTile(place)){
					case VOID:
						System.out.print("X");
						break;
					case OFF_MAP:
						System.out.print("X");
						break;
					case NORMAL:
						System.out.print(" ");
						break;
					case UNKNOWN:
						System.out.print("U");
						break;					
					}
				}else{
					System.out.print(" ");
				}
			}
			System.out.println();
		}
		System.out.println("---------------------------------------------------");
	}
	/**
	    * if there is danger, tells us how we should move
	    * @return null if no danger, or the direction away from the danger
	    */
	public Direction locateTheDanger(boolean toughGuy){
		RobotInfo[] enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
		int sumX = 0;
		int sumY = 0;
		int total = 0;
		for(RobotInfo enemy: enemies){
			if(toughGuy && !doesHeEvenLift(enemy.type))
				continue;
			if(rc.getLocation().distanceSquaredTo(enemy.location) < enemy.type.attackRadiusSquared){
				sumX +=enemy.location.x;
				sumY += enemy.location.y;
				total++;
			}
		}
		if(total == 0){
			return null;
		}else{
			MapLocation center = new MapLocation(sumX/total, sumY/total);
			return center.directionTo(rc.getLocation());
		}
	}
	
	/**
	 * runs though seeing if the passed robot type is even worth caring about
	 * @param type the type of the robot to check
	 * @return true if he lifts, false if he is puny and can be squashed like a bug
	 */
	public boolean doesHeEvenLift(RobotType type){
		if(type == RobotType.MISSILE){
			return true;
		}else if(type == RobotType.TANK){
			return true;
		}else if(type == RobotType.COMMANDER){
			return true;
		}else if(type == RobotType.HQ){
			return true;
		}else if(type == RobotType.TOWER){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Better shooting code
	 * @throws GameActionException
	 */
	public void destroy() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(40, rc.getTeam().opponent());
		RobotInfo toDestroy = null;
		int lowestHealth =100000;
		int lowestID = 1000000;
		for(RobotInfo enemy : enemies){
			if(enemy.type == RobotType.TOWER){
				toDestroy = enemy;
				break;
			}
			if(enemy.type == RobotType.HQ){
				toDestroy = enemy;
				break;
			}
			boolean beatsBest = (enemy.health < lowestHealth)? true: enemy.ID < lowestID;
			if(beatsBest){
				lowestHealth = (int) enemy.health;
				lowestID = enemy.ID;
				toDestroy = enemy;
			}
		}
		if(toDestroy != null)
			if(rc.isWeaponReady()&&rc.canAttackLocation(toDestroy.location)){
				rc.attackLocation(toDestroy.location);
			}
		
		
	}
}

