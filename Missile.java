package team079;

import battlecode.common.*;

public class Missile extends BaseRobot {
	public RobotController rc;

	public Missile(RobotController rcin){
		super(rcin);
		rc = rcin;
	}

	@Override
	public void run() throws GameActionException {
		// TODO Auto-generated method stub

	}
	public void shootWeakest() throws GameActionException{
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
			if(rc.isCoreReady()){
				if(rc.canMove(rc.getLocation().directionTo(weakestLink.location))){
					rc.move(rc.getLocation().directionTo(weakestLink.location));
				}
				else{
					rc.explode();
				}
			}
		}
	}
}
