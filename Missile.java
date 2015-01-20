package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Missile extends BaseRobot {
	public Missile(RobotController rcin) {
		super(rcin);
		// TODO Auto-generated constructor stub
	}

	public static RobotController rc;
	public static int turnsWithoutMoving;

	public static void missileInit(RobotController rcin){
		rc = rcin;
		turnsWithoutMoving = 0;
	}

	public static void runs() throws GameActionException {
		// TODO Auto-generated method stub
		explodeWeakest();
		rc.yield();
	}
	public static void explodeWeakest() throws GameActionException{
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(36, rc.getTeam().opponent());
		/*double LowestHealth = 10000;
		RobotInfo weakestLink = null;
		for(RobotInfo ri:enemiesInRange){
			if(ri.type == RobotType.TOWER){
				weakestLink = ri;
				break;
			}
			if(ri.type == RobotType.HQ){
				weakestLink = ri;
				break;
			}
			if(ri.health<LowestHealth){
				weakestLink = ri;
				LowestHealth = ri.health;
			}
		}*/
		RobotInfo weakestLink =null;
		if(enemiesInRange.length >0)
			weakestLink = enemiesInRange[0];
		else{
			
		}
		if(weakestLink != null){
			if(rc.isCoreReady()){
				if(rc.getHealth() <=1 && rc.senseNearbyRobots(2,rc.getTeam().opponent()).length >0){
					//rc.explode();
				}
				moveAsClose(rc.getLocation().directionTo(weakestLink.location));
			}
		}else{
			//moveAsCloseToDirection(rc.getLocation().directionTo(rc.senseNearbyRobots(2, rc.getTeam())[0].location).opposite());
			moveAsClose(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
		}
	}
	
	public static boolean moveAsClose(Direction toMove) throws GameActionException{
		if(rc.isCoreReady()){
			Direction[] toTry = {toMove,
					toMove.rotateLeft(),
					toMove.rotateRight(),
					toMove.rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight()
			};
			for(Direction dir:toTry){
				if(rc.canMove(dir)&&rc.isCoreReady()){
					rc.move(dir);
					return true;
				}
			}
		}	
		return false;
	}

	@Override
	public void run() throws GameActionException {
		// TODO Auto-generated method stub
		
	}
}
