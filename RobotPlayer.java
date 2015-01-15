package testplayer1;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
	public static final int MINEFACTORYCHANNEL = 0;
	public static RobotController rc;
	public static Direction toBoldlyGo;
	public static Random randall;
	public static int missionCounter;
	public static void run(RobotController rcin){
		rc = rcin;
		randall = new Random(rc.getID());
		missionCounter = 0;
		if(robotsOfTypeOnTeam(RobotType.MINERFACTORY, rc.getTeam())<4){
			toBoldlyGo = getRandomDirection();
		}else{
			toBoldlyGo = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		}
		while(true){
			try {
				supplyChain();
				switch(rc.getType()){
				case HQ:
					shootWeakest();
					if(rc.getTeamOre()>510)
						spawnUnit(RobotType.BEAVER);
					break;
				case BEAVER:
					shootWeakest();
					if(missionCounter<10){
						moveAsCloseToDirectionSafe(toBoldlyGo);
					}else{
						if(randall.nextDouble()<0.05){
							if(robotsOfTypeOnTeam(RobotType.HELIPAD, rc.getTeam())<4){
								buildUnit(RobotType.HELIPAD);
							}else if(robotsOfTypeOnTeam(RobotType.BARRACKS, rc.getTeam())<3){
								buildUnit(RobotType.BARRACKS);
							}else if(robotsOfTypeOnTeam(RobotType.TANKFACTORY, rc.getTeam())<2){
								buildUnit(RobotType.TANKFACTORY);
							}
						}else{
							moveAndMine();
						}
					}
					break;
				case MINERFACTORY:
					if(robotsOfTypeOnTeam(RobotType.MINER, rc.getTeam())<40){
						spawnUnit(RobotType.MINER);
					}
					break;
				case MINER:
					shootWeakest();
					moveAndMine();
					break;
				case TOWER:
					shootWeakest();
					break;
				case BARRACKS:
					spawnUnit(RobotType.SOLDIER);
					break;
				case SOLDIER:
					shootWeakest();
					moveAsCloseToDirection(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
					break;
				case TANKFACTORY:
					spawnUnit(RobotType.TANK);
					break;
				case TANK:
					shootWeakest();
					moveAsCloseToDirection(rc.getLocation().directionTo(rc.senseEnemyTowerLocations()[0]));
				case HELIPAD:
					spawnUnit(RobotType.DRONE);
				case DRONE:
					scanPath();
				}
			}catch(GameActionException e){
				e.printStackTrace();
			}
			rc.yield();
		}
	}
	private static void supplyChain() {
		// TODO Auto-generated method stub
		
	}
	private static int robotsOfTypeOnTeam(RobotType type, Team team) {
		RobotInfo[] Robots = rc.senseNearbyRobots(1000000, team);
		int number = 0;
		for(RobotInfo ri: Robots){
			if(ri.type==type){
				number++;
			}
		}
		return number;
	}

	public static void moveAndMine() throws GameActionException{
		if(rc.senseOre(rc.getLocation())>=1){
			if(rc.isCoreReady()&&rc.canMine()){
				rc.mine();
			}
		}else{		
			moveAsCloseToDirectionSafe(getRandomDirection());
		}
	}
	
	public static void buildUnit(RobotType toBuild) throws GameActionException{
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
				if(rc.isCoreReady()&&rc.canBuild(buildDir, toBuild)&&rc.getTeamOre()>toBuild.oreCost){			
					rc.build(buildDir, toBuild);
					unitBuilt = true;
					break;
				}
			}
			rc.yield();
		}
	}
	
	public static Direction getRandomDirection(){
		return Direction.values()[randall.nextInt(8)];
	}
	
	public static void shootWeakest() throws GameActionException{
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
	
	public static void spawnUnit(RobotType toSpawn) throws GameActionException{
		if(rc.isCoreReady()&&rc.canSpawn(Direction.NORTH, toSpawn)){					
			rc.spawn(Direction.NORTH, toSpawn);
		}
	}
	
	public static void moveAsCloseToDirection(Direction toMove) throws GameActionException{
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
					missionCounter++;
				}
			}
		}		
	}
	
	public static void moveAsCloseToDirectionSafe(Direction toMove) throws GameActionException{
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
				if(rc.canMove(dir)&&rc.isCoreReady()&&safeToMove(dir)){
					rc.move(dir);
					missionCounter++;
				}
			}
		}		
	}
	
	public static boolean safeToMove(Direction dir){
		boolean safe = true;
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		for(RobotInfo ri: enemiesInRange){
			if(rc.getLocation().add(dir).distanceSquaredTo(ri.location)<=ri.type.attackRadiusSquared){
				safe = false;
				break;
			}
		}
		return safe;
	}
	
	public static void scanPath() throws GameActionException{
		int i = 1;
		MapLocation target = null;
		while(true){
			MapLocation[] nearby = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), i*i);
			for(MapLocation near: nearby){
				if(rc.senseTerrainTile(near) == TerrainTile.UNKNOWN && rc.senseTerrainTile(near) != TerrainTile.OFF_MAP){
					target = near;
				}
			}
			if(target != null){
				break;
			}
			i++;
		}
		pathToLocation(target, 2);
	}
	
	private static void pathToLocation(MapLocation target, int threshold) throws GameActionException {
		while(rc.getLocation().distanceSquaredTo(target)>threshold){
			moveAsCloseToDirection(rc.getLocation().directionTo(target));
			rc.yield();
		}
		
	}
	
	

}
