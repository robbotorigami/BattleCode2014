package team079;

import team079.util.ComSystem;
import battlecode.common.*;



public class Drone extends BaseRobot {
	public RobotController rc;
	public boolean supplying;
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
		supplying = false; 
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
	}
	

	private void harass() {
		// TODO Auto-generated method stub

	}

	private void supplyTheLaunchers() throws GameActionException {
		
	
		RobotInfo[] launchers = robotsOnTeam(RobotType.LAUNCHER, rc.getTeam());
		if(supplying){
			for(RobotInfo ri: launchers){
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
				if(ri.supplyLevel < 1000){
					basicPathingSafe(rc.getLocation().directionTo(ri.location));
					break;
				}
			}
			if(rc.getSupplyLevel() < 1000){
				supplying = false;
			}
		}else{
			basicPathingSafe(rc.getLocation().directionTo(rc.senseHQLocation()));
			if(rc.getSupplyLevel() > 10000){
				supplying = true;
			}
		}	
		//if(rc.getSupplyLevel() < -)
		rc.yield();

	}


	private void findWaypoint() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unused")
	private void scoutMap() throws GameActionException {
		if(false) //if(ComSytem.pathingDone() && numOfDronesOfType(ID.FIND_WAYPOINT) < 1)
		{
			myID = ID.FIND_WAYPOINT;
		}
		if(ComSystem.numOfDronesOfType(ID.SCOUTING) >2){
				myID = ID.SUPPLY_MINERS;
		}

	}
	private void supplyMiners() throws GameActionException {
		RobotInfo[] miners = robotsOnTeam(RobotType.MINER, rc.getTeam());
		if(supplying){
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

				if(ri.supplyLevel < 50){
					basicPathing(rc.getLocation().directionTo(ri.location));
					break;
				}


			}
			if(rc.getSupplyLevel() < 1000){
				supplying = false;
			}
		}else{
			basicPathing(rc.getLocation().directionTo(rc.senseHQLocation()));
			if(rc.getSupplyLevel() > 10000){
				supplying = true;
			}
		}
		rc.yield();
		if(robotsOfTypeOnTeam(RobotType.LAUNCHER, rc.getTeam()) >4 && ComSystem.numOfDronesOfType(ID.SUPPLY_LAUNCHERS) <5){
			myID = ID.SUPPLY_LAUNCHERS;

		}

	}


	

}
