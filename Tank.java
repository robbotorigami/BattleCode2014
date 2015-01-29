package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Tank extends BaseRobot {
	public RobotController rc;
	public MapLocation lastWaypoint;
	public MapLocation currentWaypoint;
	public boolean beWimp;
	public boolean notDed;
	
	public Tank(RobotController rcin){
		super(rcin);
		rc = rcin;
		notDed = true;
		currentWaypoint =ourHQ;
		lastWaypoint = ourHQ;
		beWimp = true;
	}
	
	@Override
	public void run() throws GameActionException {
		if(rc.getLocation().distanceSquaredTo(currentWaypoint) <= 2){
			ComSystem.incSync(57575);
		}
		/*if(rc.getHealth() <20 && notDed){
			//we ded
			rc.broadcast(21, rc.readBroadcast(21)+1);
			notDed = false;
		}*/
		if(rc.getLocation().distanceSquaredTo(currentWaypoint) < 40 && robotsOnTeam(RobotType.TANK, 80, rc.getTeam()).length < 8){
			rc.broadcast(21,  200);
		}
		updateWaypoint();
		warMonger();
		rc.yield();

	}
	
	private void updateWaypoint() throws GameActionException {
		if(!currentWaypoint.equals(ComSystem.getLocation(199))){
			lastWaypoint = currentWaypoint;
			currentWaypoint = ComSystem.getLocation(199);
			beWimp = true;
			//rc.broadcast(59059, 0);
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
		beWimp = total < 10 && Clock.getRoundNum() < rc.getRoundLimit()-400;
		
		dartAway(); //detectAvoidDanger();
		destroy();
		supplyChain();
		basicPathingSwarm(rc.getLocation().directionTo(currentWaypoint));
	}
	
	private int howManyFriendsIsSafe() throws GameActionException{
		if(rc.canSenseLocation(currentWaypoint)){
			RobotInfo target = rc.senseRobotAtLocation(currentWaypoint);
			if(target != null){
				return (int) (13*target.health/target.type.maxHealth);
			}
		}
		return 10;
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
	
	@Override
	public void destroy() throws GameActionException {
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
		if(!beWimp ) return;
		RobotInfo[] Robots = rc.senseNearbyRobots(30, rc.getTeam().opponent());
		for(RobotInfo ri: Robots){
			if(rc.getLocation().distanceSquaredTo(ri.location) <= ri.type.attackRadiusSquared){
				basicPathingSafe(ri.location.directionTo(rc.getLocation()));
			}
		}
	}
	
	public boolean basicPathingSwarm(Direction toMove) throws GameActionException{
		//If we are close enough to the waypoint, don't even bother moving
		if(rc.getLocation().distanceSquaredTo(currentWaypoint) <= 2) return false;
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

