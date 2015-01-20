package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Launcher extends BaseRobot {
	public RobotController rc;
	public MapLocation lastWaypoint;
	public MapLocation currentWaypoint;
	//public int myID;
	
	public Launcher(RobotController rcin){
		super(rcin);
		rc = rcin;
		initBetterPathing(ourHQ);
		currentWaypoint =ourHQ;
		lastWaypoint = ourHQ;
		//int myID = robotsOfTypeOnTeam(RobotType.LAUNCHER, rc.getTeam());
	}
	
	@Override
	public void run() throws GameActionException {
		//ComSystem.sendLocation(myID+200,rc.getLocation(), true);
		if(rc.isWeaponReady() && rc.getMissileCount() >=1){
			ComSystem.incSync(57575);
		}
		//launchAtWeakest();
		//dartAway();
		updateWaypoint();
		swarmMoving();
		supplyChain();
		
		//if(shouldWeMove())
			//basicPathingSwarm(rc.getLocation().directionTo(ComSystem.getLocation(199)));
		rc.yield();
	}
	
	private void updateWaypoint() throws GameActionException {
		if(!currentWaypoint.equals(ComSystem.getLocation(199))){
			lastWaypoint = currentWaypoint;
			currentWaypoint = ComSystem.getLocation(199);
		}
		
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
	public void launchAtWeakest() throws GameActionException{
		//if(ComSystem.readSync(57575) < 10) return;
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(36, rc.getTeam().opponent());
		double LowestHealth = 10000;
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
			if(ri.health<LowestHealth && clearPath(rc.getLocation(), ri.location)){
				weakestLink = ri;
				LowestHealth = ri.health;
			}

		}

		if(weakestLink != null){
			Direction dir = rc.getLocation().directionTo(weakestLink.location);
			Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
			int launched = 0;
			for(Direction launch:toTry){
				if(rc.isWeaponReady()&&rc.canLaunch(launch)&&rc.getMissileCount()>=1){
					rc.launchMissile(launch);
					launched ++;
					if(launched >3 && weakestLink.type != RobotType.TOWER && weakestLink.type != RobotType.HQ ){
						break;
					}
				}
			}
		}
	}
	
	public void launchAnyways() throws GameActionException{
		Direction dir = rc.getLocation().directionTo(ComSystem.getLocation(199));
		Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
		for(Direction launch:toTry){
			if(rc.isWeaponReady()&&rc.canLaunch(launch)&&rc.getMissileCount()>=1){
				rc.launchMissile(launch);
				break;
			}
		}
	}
	public boolean shouldWeMove() throws GameActionException{
		MapLocation[] towers = rc.senseEnemyTowerLocations();
		MapLocation wouldMoveTo = rc.getLocation().add(rc.getLocation().directionTo(ComSystem.getLocation(199)));
		if(rc.getLocation().distanceSquaredTo(ComSystem.getLocation(199)) < 4){
			return false;
		}
		for(MapLocation loc: towers){
			if(wouldMoveTo.distanceSquaredTo(loc) <= 20){//RobotType.TOWER.attackRadiusSquared){
				return false;
			}
		}
		if(wouldMoveTo.distanceSquaredTo(rc.senseEnemyHQLocation()) <= 16){//RobotType.HQ.attackRadiusSquared){
			return false;
		}
		RobotInfo[] robots = rc.senseNearbyRobots(wouldMoveTo, 20, rc.getTeam().opponent());
		if(robots.length == 0){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean shouldWeMove(MapLocation target) throws GameActionException{
		MapLocation[] towers = rc.senseEnemyTowerLocations();
		MapLocation wouldMoveTo = rc.getLocation().add(rc.getLocation().directionTo(target));
		if(rc.getLocation().distanceSquaredTo(ComSystem.getLocation(199)) < 4){
			return false;
		}
		for(MapLocation loc: towers){
			if(wouldMoveTo.distanceSquaredTo(loc) <= 20){//RobotType.TOWER.attackRadiusSquared){
				return false;
			}
		}
		if(wouldMoveTo.distanceSquaredTo(rc.senseEnemyHQLocation()) <= 16){//RobotType.HQ.attackRadiusSquared){
			return false;
		}
		RobotInfo[] robots = rc.senseNearbyRobots(wouldMoveTo, 40, rc.getTeam().opponent());
		for(RobotInfo robot: robots){
			if(rc.getLocation().distanceSquaredTo(robot.location) < robot.type.attackRadiusSquared){
				return false;
			}
		}
		return true;
	}
	
	private void dartAway() throws GameActionException {
		RobotInfo[] Robots = rc.senseNearbyRobots(30, rc.getTeam().opponent());
		for(RobotInfo ri: Robots){
			if(rc.getLocation().distanceSquaredTo(ri.location) <= ri.type.attackRadiusSquared){
				moveAsCloseToDirection(ri.location.directionTo(rc.getLocation()));
			}
		}
	}
	
	public boolean swarmMoving() throws GameActionException{
		
		RobotInfo[] swarmMates = robotsOnTeam(RobotType.LAUNCHER, 40, rc.getTeam());
		int sumLocx = 0;
		int sumLocy = 0;
		int sumInRange = 0;
		int total = 0;
		for(RobotInfo ri: swarmMates){
			if(ri == null) break;
			sumInRange++;
			sumLocx+=ri.location.x;
			sumLocy+=ri.location.y;
			total++;
		}
		if(total == 0){
			sumLocx = rc.getLocation().x;
			sumLocy = rc.getLocation().y;
			total = 1;
		}
		MapLocation center = new MapLocation(sumLocx/total, sumLocy/total); 
		MapLocation target;
		if(rc.getMissileCount() >2){
			target = center.add(center.directionTo(currentWaypoint),4);
		}else{
			target = center.add(center.directionTo(currentWaypoint).opposite(), 4);
		}
		
		if(target.distanceSquaredTo(rc.getLocation()) > currentWaypoint.distanceSquaredTo(rc.getLocation())){
			target = currentWaypoint;
		}
		
		if(!shouldWeMove(target) ||rc.getMissileCount() >2 && Clock.getRoundNum()%2 == 0){
			launchAtWeakest();
			return false;
		}else if(shouldWeMove(target)){
			return basicPathingSwarm(rc.getLocation().directionTo(target));
		}else{
			return false;
		}
		
	}
	
	public boolean basicPathingSwarm(Direction toMove) throws GameActionException{
		if(rc.isCoreReady()){
			/*if(useBug){
				return bugPath(toMove);
			}*/
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
				RobotInfo[] launches = robotsOnTeam(RobotType.LAUNCHER, 10, rc.getTeam());
				for(RobotInfo launcher:launches){
					if(rc.getLocation().add(dir).distanceSquaredTo(launcher.location) <2 && rand.nextDouble() < 0.1){
						//badLoc = true;
					}
				}
				
				for(MapLocation loc: rc.senseEnemyTowerLocations()){
					if(wouldMoveTo.distanceSquaredTo(loc) <= 20){//RobotType.TOWER.attackRadiusSquared){
						badLoc = true;
					}
				}
				if(wouldMoveTo.distanceSquaredTo(rc.senseEnemyHQLocation()) <= 20){//RobotType.HQ.attackRadiusSquared){
					badLoc = true;
				}
				
				if(rc.canMove(dir)&&rc.isCoreReady() && !badLoc){
					if(dir != toMove){
						useBug = true;
					}
					oldLocs.add(rc.getLocation().add(dir));
					oldLocs.remove(0);
					lastDir = dir;
					lastTried = toMove;
					rc.move(dir);
					return true;
				}
			}
		}	
		return false;
	}
	
	public MapLocation[] findFrontLine(){
		return null;
	}

}
