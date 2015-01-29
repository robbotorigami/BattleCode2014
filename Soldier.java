package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Soldier extends BaseRobot {
	public RobotController rc;
	public MapLocation tower;
	public boolean beWimp;
	public MapLocation lastWaypoint;
	public MapLocation currentWaypoint;
	public boolean notDed;
	
	public Soldier(RobotController rcin){
		super(rcin);
		rc = rcin;
		notDed = true;
		currentWaypoint =ourHQ;
		lastWaypoint = ourHQ;
		int lowestDistance = 1000000;
		tower = null;
		for(MapLocation loc: rc.senseTowerLocations()){
			if(rc.getLocation().distanceSquaredTo(loc) < lowestDistance){
				lowestDistance = rc.getLocation().distanceSquaredTo(loc);
				tower = loc;
			}
		}
	}
	
	@Override
	public void run() throws GameActionException {
		/*betterPathing(rc.senseEnemyHQLocation()); 
		rc.yield();
		if(true)return;*/
		if(rc.getLocation().distanceSquaredTo(currentWaypoint) < 40){
			//ComSystem.incSync(57575);
		}
		/*if(rc.getHealth() <20 && notDed){
			//we ded
			rc.broadcast(21, rc.readBroadcast(21)+1);
			notDed = false;
		}*/
		updateWaypoint();
		dartAway();
		destroy();
		supplyChain();
		if(ComSystem.getFlag(199)){
			basicPathing(rc.getLocation().directionTo(tower));
		}else{
			//if(ComSystem.getLocation(199).distanceSquaredTo(rc.getLocation().add(rc.getLocation().directionTo(ComSystem.getLocation(199)))) <=2){
				basicPathing(rc.getLocation().directionTo(ComSystem.getLocation(199)));
			//}
		}
		rc.yield();

	}
	
	private void updateWaypoint() throws GameActionException {
		if(!currentWaypoint.equals(ComSystem.getLocation(199))){
			lastWaypoint = currentWaypoint;
			currentWaypoint = ComSystem.getLocation(199);
			beWimp = true;
			rc.broadcast(59059, 0);
		}
		
	}
	
	private void warMonger() throws GameActionException {
		/*beWimp = (!beWimp)? beWimp:robotsOnTeam(RobotType.TANK, 500, rc.getTeam()).length < 20;
		if(!beWimp){
			rc.broadcast(59059, 1);
		}
		beWimp = rc.readBroadcast(59059) == 0;*/
		int total = 0;
		RobotInfo[] bots = rc.senseNearbyRobots(currentWaypoint, 50, rc.getTeam());
		for(RobotInfo bot: bots){
			if(bot.type == RobotType.TANK){
				total++;
			}
		}
		beWimp = total < 10;
		supplyChain();
		basicPathingSwarm(rc.getLocation().directionTo(currentWaypoint));
	}
	
	private void dartAway() throws GameActionException {
		RobotInfo[] Robots = rc.senseNearbyRobots(30, rc.getTeam().opponent());
		for(RobotInfo ri: Robots){
			if(rc.getLocation().distanceSquaredTo(ri.location) <= ri.type.attackRadiusSquared){
				basicPathingSafe(ri.location.directionTo(rc.getLocation()));
			}
		}
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
