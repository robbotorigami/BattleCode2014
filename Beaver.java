package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Beaver extends BaseRobot {
	public RobotController rc;
	public int turnsMoved;
	public int buildingTries;
	public Direction dir;
	public int specialOps;
	
	public Beaver(RobotController rcin){
		super(rcin);
		rc = rcin;
		turnsMoved = 0;
		Direction[] possible = {ourHQ.directionTo(theirHQ),
				ourHQ.directionTo(theirHQ).rotateLeft(),
				ourHQ.directionTo(theirHQ).rotateRight()
		};
		dir = possible[rand.nextInt(3)];
		buildingTries = 0;
		
		specialOps = Math.max(robotsOfTypeOnTeam(RobotType.BEAVER, rc.getTeam())-2,0);
	}
	
	
	@Override
	public void run() throws GameActionException {
		supplyChain();
		if(specialOps ==0){
			schityBuilder();
		}else{
			buildTowerProtection();
		}
		rc.yield();

	}
	
	private void buildTowerProtection() throws GameActionException{
		MapLocation currentTarget;
		MapLocation[] ourTowers = rc.senseTowerLocations();
		if(specialOps == 1){
			int i = 0;
			while(!towerInNeed(ourTowers[i])){
				i++;
				if(i>ourTowers.length-1){
					specialOps =0;
					return;
				}
			}
			currentTarget = ourTowers[i].add(Direction.SOUTH);
		}else{
			int i = ourTowers.length - 1;
			while(!towerInNeed(ourTowers[i])){
				i--;
				if(i < 0){
					specialOps =0;
					return;
				}
			}
			currentTarget = ourTowers[i].add(Direction.SOUTH);
		}
		if(rc.getLocation().distanceSquaredTo(currentTarget) <= 2){
			if(rc.canBuild(rc.getLocation().directionTo(currentTarget), RobotType.BARRACKS) && rc.isCoreReady()){
				rc.build(rc.getLocation().directionTo(currentTarget), RobotType.BARRACKS);
			}
		}else{
			basicPathing(rc.getLocation().directionTo(currentTarget));
		}
	}
	private boolean towerInNeed(MapLocation tower) throws GameActionException {
		for(Direction dir: Direction.values()){
			MapLocation toCheck = tower.add(dir);
			if(rc.canSenseLocation(toCheck)&& rc.senseRobotAtLocation(toCheck) != null && rc.senseRobotAtLocation(toCheck).type == RobotType.BARRACKS){
				return false;
			}
		}
		return true;
	}


	private void schityBuilder() throws GameActionException{
		RobotType toBuild = null;
		if(robotsOfTypeOnTeam(RobotType.MINERFACTORY,rc.getTeam()) < 1){
			toBuild = RobotType.MINERFACTORY;
		} 
		else if(robotsOfTypeOnTeam(RobotType.MINERFACTORY,rc.getTeam()) < 2){
			toBuild = RobotType.MINERFACTORY;
		}
		else if(robotsOfTypeOnTeam(RobotType.BARRACKS, rc.getTeam()) < 1 + rc.senseTowerLocations().length){
			toBuild = RobotType.BARRACKS;
		}
		else if(robotsOfTypeOnTeam(RobotType.TANKFACTORY, rc.getTeam()) < 1){
			toBuild = RobotType.TANKFACTORY;
		}
		else if(robotsOfTypeOnTeam(RobotType.HELIPAD, rc.getTeam()) < 1){
			toBuild = RobotType.HELIPAD;
		}
		else if(robotsOfTypeOnTeam(RobotType.SUPPLYDEPOT, rc.getTeam()) < 4){
			toBuild = RobotType.SUPPLYDEPOT;
		}/*
		else if(robotsOfTypeOnTeam(RobotType.BARRACKS,rc.getTeam()) < 1 && rc.getLocation().distanceSquaredTo(ourHQ) >= 6*6){
			buildUnit(RobotType.BARRACKS);
		}*/
		else if(robotsOfTypeOnTeam(RobotType.BARRACKS,rc.getTeam()) < 2 + rc.senseTowerLocations().length){
			toBuild = RobotType.BARRACKS;
		}
		else if(robotsOfTypeOnTeam(RobotType.TANKFACTORY,rc.getTeam()) < 6){
			toBuild = RobotType.TANKFACTORY;
		}
		else if(robotsOfTypeOnTeam(RobotType.SUPPLYDEPOT, rc.getTeam()) < 10 && robotsOfTypeOnTeam(RobotType.TANK, rc.getTeam()) > 3* robotsOfTypeOnTeam(RobotType.SUPPLYDEPOT, rc.getTeam())){
			toBuild = RobotType.SUPPLYDEPOT;
		}/*
		else if(robotsOfTypeOnTeam(RobotType.BARRACKS,rc.getTeam()) < 2 && rc.getLocation().distanceSquaredTo(ourHQ) >= 6*6 && robotsOfTypeOnTeam(RobotType.LAUNCHER, rc.getTeam()) > 6){
			buildUnit(RobotType.BARRACKS);
		}*/
		boolean haveOre = (toBuild == null)? true: rc.getTeamOre() > toBuild.oreCost;
		if(rc.isCoreReady()){
			if(!buildUnit(toBuild) && buildingTries > 10 && haveOre){
				if(basicPathing(dir)){
					buildingTries = 0;
				}
			}else{
				buildingTries++;
			}
		}
		rc.setIndicatorString(0, buildingTries+"");
	}
	
	private void supplyChain() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<200){
				int toSupply = 0;
				toSupply = (int) (Math.abs((rc.getSupplyLevel()-ri.supplyLevel))/2);
				if(ri.type == RobotType.HQ){
					toSupply = 0;
				}
				if(rc.senseRobotAtLocation(ri.location) != null){
					rc.transferSupplies(toSupply, ri.location);
					break;
				}
			}
			
		}
	}

}

