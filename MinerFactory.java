package team079;

import battlecode.common.*;

public class MinerFactory extends BaseRobot {
	public RobotController rc;
	
	public MinerFactory(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		spawnUnit(RobotType.MINER);
		rc.yield();
	}

}
