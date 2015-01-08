package testplayer2;

import battlecode.common.*;

public class HQ extends BaseRobot {
	public RobotController rc;
	public int beaverCount;
	
	public HQ(RobotController rcin){
		super(rcin);
		rc = rcin;
		beaverCount = 0;
	}
	
	@Override
	public void run() throws GameActionException {
		
		Direction dir = getRandomDirection();
		if(rc.isCoreReady()&&rc.canSpawn(dir , RobotType.BEAVER)&&beaverCount<3){
			
			rc.spawn(dir, RobotType.BEAVER);
			beaverCount++;
		}
		rc.yield();

	}

}
