package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Launcher extends BaseRobot {
	public RobotController rc;
	//public int myID;
	
	public Launcher(RobotController rcin){
		super(rcin);
		rc = rcin;
		//int myID = robotsOfTypeOnTeam(RobotType.LAUNCHER, rc.getTeam());
	}
	
	@Override
	public void run() throws GameActionException {
		//ComSystem.sendLocation(myID+200,rc.getLocation(), true);
		launchAtWeakest();
		supplyChain();
		if(shouldWeMove())
			basicPathing(rc.getLocation().directionTo(ComSystem.getLocation(199)));
		rc.yield();
	}
	
	private void supplyChain() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<rc.getSupplyLevel()*0.75){
				int toSupply = 0;				
				if(ri.type == RobotType.LAUNCHER){
					toSupply = (int) ((rc.getSupplyLevel()-ri.supplyLevel)/2);
				}
				if(rc.senseRobotAtLocation(ri.location) != null){
					if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){
						rc.transferSupplies(toSupply, ri.location);
					}
				}
			}
			
		}
	}
	public void launchAtWeakest() throws GameActionException{
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(36, rc.getTeam().opponent());
		double LowestHealth = 10000;
		RobotInfo weakestLink = null;
		for(RobotInfo ri:enemiesInRange){
			if(ri.type == RobotType.TOWER){
				weakestLink = ri;
				break;
			}
			if(ri.health<LowestHealth && clearPath(rc.getLocation(), ri.location)){
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
	public boolean shouldWeMove() throws GameActionException{
		MapLocation[] towers = rc.senseEnemyTowerLocations();
		MapLocation wouldMoveTo = rc.getLocation().add(rc.getLocation().directionTo(ComSystem.getLocation(199)));
		for(MapLocation loc: towers){
			if(wouldMoveTo.distanceSquaredTo(loc) <= RobotType.TOWER.attackRadiusSquared){
				return false;
			}
		}
		if(wouldMoveTo.distanceSquaredTo(rc.senseEnemyHQLocation()) <= RobotType.HQ.attackRadiusSquared){
			return false;
		}
		RobotInfo[] robots = rc.senseNearbyRobots(25, rc.getTeam().opponent());
		if(robots.length == 0){
			return true;
		}else{
			return false;
		}
	}
	

}
