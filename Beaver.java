package testplayer2;

import battlecode.common.*;

public class Beaver extends BaseRobot {
	public RobotController rc;
	public int timesMoved;
	public Beaver(RobotController rcin){
		super(rcin);
		rc = rcin;
		timesMoved = 0;
	}
	
	@Override
	
	public void run() throws GameActionException {
		Direction dir = getRandomDirection();
		if(rc.isCoreReady()&&rc.canMove(dir)&&timesMoved<6){
			rc.move(dir);
			
		}
			rc.yield();

	}

}
