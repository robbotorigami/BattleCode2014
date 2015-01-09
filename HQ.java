package testplayer2;

import battlecode.common.*;

public class HQ extends BaseRobot {
	
	public RobotController rc;


	public HQ(RobotController rcin){
		super(rcin);
		numberOfBeavers = 0;
		rc = rcin;
	}

	Direction dir = getRandomDirection();
	public void run() throws GameActionException {
		if(rc.isCoreReady()&&rc.canSpawn(dir, RobotType.BEAVER)){

			rc.spawn(dir, RobotType.BEAVER);
		}
	}
}
