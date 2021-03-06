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
		SUPPLY_MINERS,SUPPLY_TANKS,
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
		rc.setIndicatorString(0, ""+ myID);
		ComSystem.handleDroneID(myID);
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
		case SUPPLY_TANKS:
			supplyTheTanks();
			break;
		case HARASS:
			harass();
			break;
		}

		rc.yield();
	}
	

	private void harass() {
		// TODO Auto-generated method stub

	}

	private void supplyTheTanks() throws GameActionException {
		
	
		RobotInfo[] tanks = robotsOnTeam(RobotType.TANK, rc.getTeam());
		if(supplying){
			RobotInfo closestToWaypoint = null;
			int bestDisToWaypoint = 1000000;
			for(RobotInfo ri: tanks){
				if(ri.supplyLevel < 3000 && rc.getLocation().distanceSquaredTo(ri.location) < bestDisToWaypoint){
					closestToWaypoint = ri;
					bestDisToWaypoint =  rc.getLocation().distanceSquaredTo(ri.location);
					break;
				}
			}
			if(closestToWaypoint != null){
				basicPathingSafe(rc.getLocation().directionTo(closestToWaypoint.location));
			}

			if(rc.getLocation().distanceSquaredTo(ComSystem.getLocation(199)) < 60){
				for(RobotInfo ri: tanks){
					int toSupply = 0;
					//if(rc.getLocation().distanceSquaredTo(ComSystem.getLocation(199)) < 50)
						toSupply = Math.max((int) rc.getSupplyLevel() - 150,0);
					/*else if(ri.supplyLevel < 2000)
						toSupply = (int) Math.min(2000, rc.getSupplyLevel()/2);*/
					if(rc.senseRobotAtLocation(ri.location) != null){
						if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){
							if(ri.location.distanceSquaredTo(rc.getLocation())< 15){
								rc.transferSupplies(toSupply, ri.location);
								break;
							}

						}

					}
				}
			}
			if(rc.getSupplyLevel() < 1000){
				supplying = false;
			}
		}else{
			basicPathingSafe(rc.getLocation().directionTo(rc.senseHQLocation()));
			if(rc.getSupplyLevel() > 5000){
				supplying = true;
			}
		}	
		//if(rc.getSupplyLevel() < -)

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
		if(ComSystem.numOfDronesOfType(ID.SCOUTING) >-1000){
				myID = ID.SUPPLY_MINERS;
		}

	}
	private void supplyMiners() throws GameActionException {
		RobotInfo[] miners = robotsOnTeam(RobotType.MINER, rc.getTeam());
		if(supplying){
			for(RobotInfo ri: miners){
				int toSupply = 0;
				toSupply = Math.max((int) rc.getSupplyLevel() - 150,0)/20;
				if(rc.senseRobotAtLocation(ri.location) != null){
					if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){

						if(ri.location.distanceSquaredTo(rc.getLocation())< 15){
							rc.transferSupplies(toSupply, ri.location);
							break;
						}

					}

				}

				if(ri.supplyLevel < 50){
					basicPathingSafe(rc.getLocation().directionTo(ri.location));
					break;
				}


			}
			if(rc.getSupplyLevel() < 250){
				supplying = false;
			}
		}else{
			basicPathingSafe(rc.getLocation().directionTo(rc.senseHQLocation()));
			if(rc.getSupplyLevel() > 1000){
				supplying = true;
			}
		}
		if( ComSystem.numOfDronesOfType(ID.SUPPLY_TANKS) <4 || robotsOfTypeOnTeam(RobotType.TANK, rc.getTeam()) >8){
			myID = ID.SUPPLY_TANKS;
			ComSystem.handleDroneID(myID);
			System.out.println(ComSystem.numOfDronesOfType(ID.SUPPLY_TANKS));
		}
		

	}


	

}

