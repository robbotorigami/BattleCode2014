package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class HQ extends BaseRobot {
	public RobotController rc;
	public int lastTime;
	public MapLocation[] waypoints;
	public int currentWaypoint;
	public final int WAYPOINTMAXDISTANCE = 10;
	public final int SWARMCHANNEL = 199;
	public final int SWARMDISTANCE = 10;
	public final int SWARMAMOUNT = 10;

	public HQ(RobotController rcin){
		super(rcin);
		rc = rcin;
		try {
			initSwarm();
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() throws GameActionException {
		ComSystem.clearUselessMiners();
		ComSystem.clearMiningInfo();
		handleSwarm();
		supplyChain();
		shootWeakest();
		if(robotsOfTypeOnTeam(RobotType.BEAVER,rc.getTeam()) < 3){
			spawnUnit(RobotType.BEAVER);
		}
		if(robotsOfTypeOnTeam(RobotType.BEAVER,rc.getTeam()) < 10 && rc.getTeamOre() >600){
			spawnUnit(RobotType.BEAVER);
		}
		rc.yield();
	}
	
	public void initSwarm()throws GameActionException{
		int actWaypointDis = (int) Math.min(Math.sqrt(rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()))/2,WAYPOINTMAXDISTANCE);
		int numWaypoints = 2;
		waypoints = new MapLocation[numWaypoints];
		waypoints[0] = rc.getLocation().add(rc.getLocation().directionTo(rc.senseEnemyHQLocation()), actWaypointDis);
		waypoints[1] = rc.senseEnemyHQLocation();
		currentWaypoint = 0;
	}
	
	public void handleSwarm() throws GameActionException{
		if(Clock.getRoundNum() > 800){
			if(robotsOfTypeOnTeam(RobotType.LAUNCHER, rc.getTeam())>SWARMAMOUNT){
				if(robotsAtWaypoint() && currentWaypoint < waypoints.length-1){
					currentWaypoint++;
				}
			}
			ComSystem.sendLocation(199, waypoints[currentWaypoint], false);
		}else{
			RobotInfo[] drones = robotsOnTeam(RobotType.DRONE, rc.getTeam().opponent());
			int lowestID = 10000;
			RobotInfo target = null;
			for(RobotInfo drone: drones){
				if(drone != null && drone.ID < lowestID){
					target = drone;
					lowestID = drone.ID;
				}
			}
			if(target != null){
				ComSystem.sendLocation(199, target.location, false);
			}else{
				ComSystem.sendLocation(199, waypoints[currentWaypoint], false);
			}
		}
	}

	public boolean robotsAtWaypoint(){
		int sumInRange = 0;
		int sumLocx = 0;
		int sumLocy = 0;
		int total = 0;
		for(RobotInfo ri: robotsOnTeam(RobotType.LAUNCHER, rc.getTeam())){
			if(ri == null) break;
			sumLocx+=ri.location.x;
			sumLocy+=ri.location.y;
			if(ri.location.distanceSquaredTo(waypoints[currentWaypoint]) < SWARMDISTANCE){
				sumInRange++;
			}
			total++;
		}
		MapLocation center = new MapLocation(sumLocx/total, sumLocy/total);
		if(sumInRange> 0.7*SWARMAMOUNT){
			return true;
		}
		return false;
	}
	
	private void supplyChain() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<200){
				int toSupply = 0;
				toSupply = Math.max((int) (Math.abs((rc.getSupplyLevel()-ri.supplyLevel)/16)), 200);
				if(ri.type == RobotType.DRONE){
					toSupply = (int)rc.getSupplyLevel()/2;
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
