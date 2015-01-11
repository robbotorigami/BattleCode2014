package team079;

import battlecode.common.*;

public class Missile extends BaseRobot {
	public RobotController rc;
	public int turnsWithoutMoving;

	public Missile(RobotController rcin){
		super(rcin);
		rc = rcin;
		turnsWithoutMoving = 0;
	}

	@Override
	public void run() throws GameActionException {
		// TODO Auto-generated method stub
		explodeWeakest();
		rc.yield();
	}
	public void explodeWeakest() throws GameActionException{
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(36, rc.getTeam().opponent());
		double LowestHealth = 10000;
		RobotInfo weakestLink = null;
		for(RobotInfo ri:enemiesInRange){
			if(ri.type == RobotType.TOWER){
				weakestLink = ri;
				break;
			}
			if(ri.health<LowestHealth){
				weakestLink = ri;
				LowestHealth = ri.health;
			}
		}

		if(weakestLink != null){
			if(rc.isCoreReady()){
				if(basicPathing(rc.getLocation().directionTo(weakestLink.location))){					
					turnsWithoutMoving = 0;
				}
				else{
					turnsWithoutMoving++;
					if(turnsWithoutMoving >2){
						rc.explode();
					}
				}
			}
		}
	}
}
