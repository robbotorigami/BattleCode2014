package testplayer2;

import battlecode.common.*;

public abstract class BaseRobot {
	public RobotController rc;
	public BaseRobot(RobotController rcin){
		rc = rcin;
	}
	
	//Abstract method for major functionality
	public abstract void run() throws GameActionException;
	
	
}
