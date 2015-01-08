package team079;

import battlecode.common.*;

public class HQ extends BaseRobot {
	public RobotController rc;
	public int lastTime;
	public MapLocation waypoint;

	public HQ(RobotController rcin){
		super(rcin);
		rc = rcin;
		
		try {
			ComSystem.sendLocation(10, rc.getLocation(), false);
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lastTime = 200;
		waypoint = rc.getLocation();
	}
	
	@Override
	public void run() throws GameActionException {
		if(Clock.getRoundNum()>lastTime+20){
			waypoint.add(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
			ComSystem.sendLocation(10, waypoint, false);
		}
		spawnUnit(RobotType.BEAVER);
		rc.yield();
	}

}
