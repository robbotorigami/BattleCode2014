package team079;

import battlecode.common.*;

public class Tower extends BaseRobot {
	public RobotController rc;
	
	public Tower(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		shootWeakest();
		rc.yield();

	}

}
