package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Soldier extends BaseRobot {
	public RobotController rc;
	
	public Soldier(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		betterPathing(rc.senseEnemyHQLocation()); 
		rc.yield();
		if(true)return;
		shootWeakest();
		supplyChain();
		basicPathing(rc.getLocation().directionTo(ComSystem.getLocation(199).add(ourHQ.directionTo(theirHQ),3)));
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
				if(ri.type == RobotType.SOLDIER){
					toSupply = (int) ((rc.getSupplyLevel() - ri.supplyLevel)/4);
				}
				if(rc.senseRobotAtLocation(ri.location) != null && toSupply !=0){
					rc.transferSupplies(toSupply, ri.location);
					break;
				}
			}
			
		}
	}

}
