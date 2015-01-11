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
	
	public boolean shouldWeMove(){
		MapLocation[] towers = rc.senseEnemyTowerLocations();
		for(MapLocation loc: towers){
			if(rc.getLocation().distanceSquaredTo(loc) <= 25){
				return false;
			}
		}
		if(rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()) <= 25){
			return false;
		}
		
		return true;
	}
	

}
