package team079;

import battlecode.common.*;

public class Basher extends BaseRobot {
	public RobotController rc;
	public MapLocation tower;
	
	public Basher(RobotController rcin){
		super(rcin);
		rc = rcin;
		
		int lowestDistance = 1000000;
		tower = null;
		for(MapLocation loc: rc.senseTowerLocations()){
			if(rc.getLocation().distanceSquaredTo(loc) < lowestDistance){
				lowestDistance = rc.getLocation().distanceSquaredTo(loc);
				tower = loc;
			}
		}
		System.out.println(rc.getLocation());
		rc.yield();
	}
	
	@Override
	public void run() throws GameActionException {
		bullRush();
		bullPen();
	}

	private void bullRush() throws GameActionException {
		int smallestDistance = 100000;
		RobotInfo redCape = null;
		for(RobotInfo enemy:rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent())){
			if(enemy.type != RobotType.MISSILE){
				if(smallestDistance > enemy.location.distanceSquaredTo(rc.getLocation())){
					redCape = enemy;
					smallestDistance = enemy.location.distanceSquaredTo(rc.getLocation());
				}
			}
		}
		if(redCape != null){
			moveAsCloseToDirection(rc.getLocation().directionTo(redCape.location));
		}
		
	}
	
	private void bullPen() throws GameActionException{
		Direction toMove = rc.getLocation().directionTo(tower);
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
				}
			}
		}
	}

}
