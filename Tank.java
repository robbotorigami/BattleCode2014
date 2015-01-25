package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Tank extends BaseRobot {
	public RobotController rc;
	public MapLocation lastWaypoint;
	public MapLocation currentWaypoint;
	
	public Tank(RobotController rcin){
		super(rcin);
		rc = rcin;
		currentWaypoint =ourHQ;
		lastWaypoint = ourHQ;
	}
	
	@Override
	public void run() throws GameActionException {
		updateWaypoint();
		warMonger();
		rc.yield();

	}
	
	private void updateWaypoint() throws GameActionException {
		if(!currentWaypoint.equals(ComSystem.getLocation(199))){
			lastWaypoint = currentWaypoint;
			currentWaypoint = ComSystem.getLocation(199);
		}
		
	}

	private void warMonger() throws GameActionException {
		dartAway(); //detectAvoidDanger();
		destroy();
		supplyChain();
		basicPathingSwarm(rc.getLocation().directionTo(currentWaypoint));
	}
	
	private void supplyChain() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<rc.getSupplyLevel()*0.75){
				int toSupply = 0;				
				if(ri.type == RobotType.TANK){
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
	
	private void destroy() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(40, rc.getTeam().opponent());
		RobotInfo toDestroy = null;
		int lowestHealth =100000;
		int lowestID = 1000000;
		for(RobotInfo enemy : enemies){
			if(enemy.type == RobotType.TOWER){
				toDestroy = enemy;
				break;
			}
			if(enemy.type == RobotType.HQ){
				toDestroy = enemy;
				break;
			}
			boolean beatsBest = (enemy.health < lowestHealth)? true: enemy.ID < lowestID;
			if(beatsBest){
				lowestHealth = (int) enemy.health;
				lowestID = enemy.ID;
				toDestroy = enemy;
			}
		}
		if(toDestroy != null)
			if(rc.isWeaponReady()&&rc.canAttackLocation(toDestroy.location)){
				rc.attackLocation(toDestroy.location);
			}
		
		
	}

	private void detectAvoidDanger() throws GameActionException {
		Direction dangerWillRobinson = locateTheDanger(false);
		if(dangerWillRobinson != null){
			basicPathing(dangerWillRobinson);
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
	
	public boolean basicPathingSwarm(Direction toMove) throws GameActionException{
		if(rc.isCoreReady()){
			oldLocs.remove(0);
			while(oldLocs.size() < NUMOLDLOCS){
				oldLocs.add(new MapLocation(0,0));
			}
			Direction[] toTry = {toMove,
					toMove.rotateLeft(),
					toMove.rotateRight(),
					toMove.rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight(),
					toMove.rotateLeft().rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight().rotateRight(),
					toMove.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
			};		
			
			for(Direction dir:toTry){
				boolean badLoc = false;
				MapLocation wouldMoveTo = rc.getLocation().add(dir);
				if(oldLocs.contains(rc.getLocation().add(dir))){
					badLoc = true;
				}
				
				boolean beWimp = robotsOnTeam(RobotType.TANK, 100, rc.getTeam()).length < 40;
				for(MapLocation loc: rc.senseEnemyTowerLocations()){
					if(wouldMoveTo.distanceSquaredTo(loc) <= 24 && beWimp){//RobotType.TOWER.attackRadiusSquared){
						badLoc = true;
					}
				}
				if(wouldMoveTo.distanceSquaredTo(rc.senseEnemyHQLocation()) <= 24 && beWimp){//RobotType.HQ.attackRadiusSquared){
					badLoc = true;
				}
				rc.setIndicatorString(2, badLoc +"");
				if(rc.canMove(dir)&&rc.isCoreReady() && !badLoc){
					rc.setIndicatorString(1, dir.toString() + ", " + Clock.getRoundNum());
					oldLocs.add(rc.getLocation().add(dir));
					lastDir = dir;
					lastTried = toMove;
					rc.move(dir);
					return true;
				}
			}
		}	
		return false;
	}

}
