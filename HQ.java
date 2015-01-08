package team079;

import battlecode.common.*;

public class HQ extends BaseRobot {
	public RobotController rc;
	
	public HQ(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		doNothing();

	}

}
