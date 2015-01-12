package team079;

import team079.util.ComSystem;
import battlecode.common.*;



public class Drone extends BaseRobot {
	public RobotController rc;
	public boolean supplyingLaunchers;
	public boolean areWeAnnoying;
	public boolean areWeMeanderer;
	public Drone(RobotController rcin){
		super(rcin);
		rc = rcin;
		supplyingLaunchers = false;
		try {
			rc.broadcast(2099, rc.readBroadcast(2099)+1);
			areWeAnnoying = rc.readBroadcast(2099) <15;
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		areWeAnnoying =false;
		
		try {
			rc.broadcast(2990, rc.readBroadcast(2990)+1);
			areWeMeanderer = rc.readBroadcast(2990) <=1;
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() throws GameActionException {
		if(!areWeAnnoying){
			shootWeakest();
			supplyLaunchers();
			if(supplyingLaunchers){
				basicPathing(rc.getLocation().directionTo(ComSystem.getLocation(199)));
				if(rc.getSupplyLevel()<200){
					supplyingLaunchers = false;				
				}

			}
			else{
				if(rc.getSupplyLevel()>1000){
					supplyingLaunchers = true;
				}
				basicPathing(rc.getLocation().directionTo(rc.senseHQLocation()));
			}

			rc.yield();
		}else{
			shootWeakest();
			rc.yield();
			if(Clock.getRoundNum() <500){
				dartAway();
				if(rc.getLocation().distanceSquaredTo(theirHQ)> Math.pow(Math.sqrt(RobotType.HQ.attackRadiusSquared)+1.5, 2))
					basicPathingSafe(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
			} else{
				basicPathing(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
			}
			rc.yield();
		}



	}
	
	private void dartAway() throws GameActionException {
		RobotInfo[] Robots = rc.senseNearbyRobots(30, rc.getTeam().opponent());
		for(RobotInfo ri: Robots){
			if(rc.getLocation().distanceSquaredTo(ri.location) <= ri.type.attackRadiusSquared){
				moveAsCloseToDirection(ri.location.directionTo(rc.getLocation()));
			}
		}
		
	}

	private void supplyLaunchers() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.type==RobotType.LAUNCHER || ri.type == RobotType.SOLDIER){
				int toSupply = 0;
				if(rc.getLocation().distanceSquaredTo(ComSystem.getLocation(199))<40){
					toSupply= Math.max((int) rc.getSupplyLevel() - 150,0);
				}else{
					toSupply = (int) Math.max(Math.min(rc.getSupplyLevel(),100) - 150,0);
				}

				if(rc.senseRobotAtLocation(ri.location) != null){
					if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){
						rc.transferSupplies(toSupply, ri.location);
						break;
					}
				}
			}

		}
	}

}
