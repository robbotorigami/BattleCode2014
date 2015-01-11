package team079;

import team079.util.ComSystem;
import battlecode.common.*;



public class Drone extends BaseRobot {
	public RobotController rc;
	public boolean supplyingLaunchers;
	public Drone(RobotController rcin){
		super(rcin);
		rc = rcin;
		supplyingLaunchers = false;
	}

	@Override
	public void run() throws GameActionException {
		shootWeakest();
		if(supplyingLaunchers){
			if(rc.getLocation().distanceSquaredTo(ComSystem.getLocation(199))<40){
				supplyLaunchers();
			}
			basicPathing(rc.getLocation().directionTo(ComSystem.getLocation(199)));
			if(rc.getSupplyLevel()<200){
				supplyingLaunchers = false;
				
			}

		}
		else{
			if(rc.getSupplyLevel()>200){
				supplyingLaunchers = true;
			}
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
