package team079;

import team079.util.ComSystem;
import battlecode.common.*;



public class Drone extends BaseRobot {
	public RobotController rc;
	public boolean supplyingLaunchers;
	public boolean areWeAnnoying;
	public boolean areWeMeanderer;
	public MapLocation center;
	public enum ID{
		SCOUTING,FIND_WAYPOINT,
		SUPPLY_MINERS,SUPPLY_LAUNCHERS,
		HARASS
	}
	public ID myID;
	public Drone(RobotController rcin){
		super(rcin);
		rc = rcin;
		supplyingLaunchers = false; 
		myID = ID.SCOUTING;
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
		center = new MapLocation(0,0);
	}

	@Override
	public void run() throws GameActionException {
		switch(myID){
		case SCOUTING:
			scoutMap();
			break;
		case FIND_WAYPOINT:
			findWaypoint();
			break;
		case SUPPLY_MINERS:
			supplyMiners();
			break;
		case SUPPLY_LAUNCHERS:
			supplyTheLaunchers();
			break;
		case HARASS:
			harass();
			break;
		}

		shootWeakest();
		if(Clock.getRoundNum() > 800){
			if(!areWeAnnoying){
				shootWeakest();
				supplyLaunchers();
				if(supplyingLaunchers){
					int sumLocx = 0;
					int sumLocy = 0;
					int total = 0;
					for(RobotInfo ri: robotsOnTeam(RobotType.LAUNCHER, rc.getTeam())){
						if(ri == null) break;
						if(ri.location.distanceSquaredTo(ComSystem.getLocation(199)) < 40){
							sumLocx+=ri.location.x;
							sumLocy+=ri.location.y;
							total++;
						}
					}
					if(total != 0)
						center = new MapLocation(sumLocx/total, sumLocy/total);
					basicPathing(rc.getLocation().directionTo(center));
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
		}else {
			RobotInfo[] drones = robotsOnTeam(RobotType.DRONE, rc.getTeam().opponent());
			int closest = 1000000000;
			RobotInfo target = null;
			for(RobotInfo drone: drones){
				if(drone != null && drone.location.distanceSquaredTo(rc.getLocation())< closest){
					target = drone;
					closest = drone.location.distanceSquaredTo(rc.getLocation());
				}
			}
			if(target != null){
				basicPathing(rc.getLocation().directionTo(target.location));
			}
		}
	}

	private void harass() {
		// TODO Auto-generated method stub

	}

	private void supplyTheLaunchers() {
		// TODO Auto-generated method stub

	}


	private void findWaypoint() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unused")
	private void scoutMap() {
		if(false) //if(ComSytem.pathingDone() && numOfDronesOfType(ID.FIND_WAYPOINT) < 1)
		{
			myID = ID.FIND_WAYPOINT;	
		}


	}
	private void supplyMiners() throws GameActionException {
		RobotInfo[] miners = robotsOnTeam(RobotType.MINER, rc.getTeam());
		for(RobotInfo ri: miners){
			int toSupply = 0;
			toSupply = Math.max((int) rc.getSupplyLevel() - 150,0);
			if(rc.senseRobotAtLocation(ri.location) != null){
				if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){
					
					if(ri.location.distanceSquaredTo(rc.getLocation())< 15){
							rc.transferSupplies(toSupply, ri.location);
							break;
					}
					
				}

			}
			if(rc.getSupplyLevel() > 250){
				if(ri.supplyLevel < 50){
					basicPathing(rc.getLocation().directionTo(ri.location));
					break;
				}
			}else{
				basicPathing(rc.getLocation().directionTo(rc.senseHQLocation()));
			}	

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
				if(rc.getLocation().distanceSquaredTo(center)<40){
					toSupply= Math.max((int) rc.getSupplyLevel() - 150,0);
				}else{
					toSupply = (int) Math.max(Math.min(rc.getSupplyLevel(),200) - 150,0);
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
