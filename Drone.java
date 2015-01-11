package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Drone extends BaseRobot {
	public RobotController rc;
	public Drone(RobotController rcin){
		super(rcin);
		rc = rcin;
	
	}
	
	@Override
	public void run() throws GameActionException {
	if(rc.getSupplyLevel()>=200){
		supplyLaunchers();
		basicPathing(rc.getLocation().directionTo(ComSystem.getLocation(199)));
	}
	if(rc.getSupplyLevel()<200){
		
		basicPathing(rc.getLocation().directionTo(rc.senseHQLocation()));
		
	}
	
		rc.yield();
		
		

	}
	private void supplyLaunchers() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.type==RobotType.LAUNCHER){
				int toSupply= (int) rc.getSupplyLevel();
				
				if(rc.senseRobotAtLocation(ri.location) != null){
					if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){
						rc.transferSupplies(toSupply, ri.location);
					}
				}
			}
			
		}
	}

}
