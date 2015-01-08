package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class HQ extends BaseRobot {
	public RobotController rc;
	public int lastTime;
	public MapLocation waypoint;

	public HQ(RobotController rcin){
		super(rcin);
		rc = rcin;
		
		try {
			ComSystem.sendLocation(10, rc.getLocation().add(rc.getLocation().directionTo(rc.senseEnemyHQLocation()),10), false);
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lastTime = 600;
		waypoint = rc.getLocation();
	}
	
	@Override
	public void run() throws GameActionException {
		if(Clock.getRoundNum()>lastTime+20){
			waypoint = waypoint.add(waypoint.directionTo(rc.senseEnemyHQLocation()));
			ComSystem.sendLocation(10, waypoint, false);
		}
		if(robotsOfTypeOnTeam(RobotType.BEAVER,rc.getTeam()) < 1){
			spawnUnit(RobotType.BEAVER);
		}
		rc.yield();
	}

}
