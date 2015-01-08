package team079;

import battlecode.common.*;

public class Beaver extends BaseRobot {
	public RobotController rc;
	
	public Beaver(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		moveAsCloseToDirection(rc.getLocation().directionTo(ComSystem.getLocation(10)));
	}

}
