package team079;

import battlecode.common.*;

public abstract class BaseRobot {
	public RobotController rc;
	public BaseRobot(RobotController rcin){
		rc = rcin;
	}
	
	public abstract void run() throws GameActionException;
	
	
}
