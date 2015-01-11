package team079;

import battlecode.common.*;

public class AerospaceLab extends BaseRobot {
	public RobotController rc;
	
	public AerospaceLab(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		// TODO Auto-generated method stub
		spawnUnit(RobotType.LAUNCHER);
	}

}
