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
					spawnUnit(RobotType.BEAVER);
					break;
				case BEAVER:
					shootWeakest();
					if(missionCounter<10){
						moveAsCloseToDirectionSafe(toBoldlyGo);
					}else{
						if(randall.nextDouble()<0.05){
							if(robotsOfTypeOnTeam(RobotType.MINERFACTORY, rc.getTeam())<4){
								buildUnit(RobotType.MINERFACTORY);
							}else{
								buildUnit(RobotType.BARRACKS);
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
				}
			}catch(GameActionException e){
				e.printStackTrace();
			}
			rc.yield();
		}
	}
	private static void supplyChain() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<rc.getSupplyLevel()/2){
				int toSupply = 0;
				if(ri.type==RobotType.BEAVER){
					toSupply = (int) ((rc.getSupplyLevel()-ri.supplyLevel)/4);
				}else{
					toSupply = (int) ((rc.getSupplyLevel()-ri.supplyLevel)/2);
				}
				if(rc.senseRobotAtLocation(rc.getLocation()).team == rc.getTeam()){
					rc.transferSupplies(toSupply, ri.location);
				}
			}
		}
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
		if(rc.senseOre(rc.getLocation())>3){
			if(rc.isCoreReady()&&rc.canMine()){
				rc.mine();
			}
		}else{		
			moveAsCloseToDirectionSafe(getRandomDirection());
		}
	}
	
	public static void buildUnit(RobotType toBuild) throws GameActionException{
		Direction dir = getRandomDirection();
		System.out.println(rc.isCoreReady()+ "     "+rc.canBuild(dir, toBuild)+ "     "+(rc.getTeamOre()>toBuild.oreCost));
		if(rc.isCoreReady()&&rc.canBuild(dir, toBuild)&&rc.getTeamOre()>toBuild.oreCost){			
			rc.build(dir, toBuild);			
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

}
