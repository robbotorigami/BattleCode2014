package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class HQ extends BaseRobot {
	public RobotController rc;
	public int lastTime;
	public MapLocation[] waypoints;
	public int currentWaypoint;
	public final int WAYPOINTDISTANCE = 10;
	public final int SWARMCHANNEL = 199;
	public final int SWARMDISTANCE = 10;
	public final int SWARMAMOUNT = 20;

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
		ComSystem.clearMiningInfo();
		if(Clock.getRoundNum()>lastTime+20){
			//waypoint = waypoint.add(waypoint.directionTo(rc.senseEnemyHQLocation()));
			//ComSystem.sendLocation(10, waypoint, false);
		}
		if(robotsOfTypeOnTeam(RobotType.BEAVER,rc.getTeam()) < 1){
			spawnUnit(RobotType.BEAVER);
		}
		System.out.println(ComSystem.getMiningLoc());
		rc.yield();
	}
	
	public void initSwarm()throws GameActionException{
		int numWaypoints = (int) (Math.sqrt(rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()))/WAYPOINTDISTANCE);
		waypoints = new MapLocation[numWaypoints];
		
		for(int i = 0; i<numWaypoints; i++){
			waypoints[i] = rc.getLocation().add(rc.getLocation().directionTo(rc.senseEnemyHQLocation()), i*WAYPOINTDISTANCE);
		}
		currentWaypoint = 0;
	}
	
	public void handleSwarm() throws GameActionException{
		if(robotsOfTypeOnTeam(RobotType.LAUNCHER, rc.getTeam())>SWARMAMOUNT){
			//if(robotsAtWaypoint)
		}
	}
	
	public boolean robotsAtWaypoint(){
		int sumInRange;
		int sumLocx = 0;
		int sumLocy = 0;
		int total = 0;
		for(RobotInfo ri: robotsOnTeam(RobotType.LAUNCHER, rc.getTeam())){
			sumLocx+=ri.location.x;
			sumLocy+=ri.location.y;
			if(ri.location.distanceSquaredTo(waypoints[currentWaypoint]) < SWARMDISTANCE){
				sumInRange++;
			}
		}
		MapLocation center = new MapLocation(sumLocx/total, sumLocy/total);
		//if(sumInRange)
	}

}
