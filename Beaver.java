package testplayer2;

import battlecode.common.*;

public class Beaver extends BaseRobot {
	public RobotController rc;
	
	public Beaver(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	
	public void run() throws GameActionException {
		Direction dir = getRandomDirection();
		if(rc.isCoreReady()&&rc.canMove(dir)){
			rc.move(dir);
			
		}
			rc.yield();

	}

}
