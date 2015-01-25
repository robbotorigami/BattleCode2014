package team079;

import battlecode.common.*;

public class TankFactory extends BaseRobot {
	public RobotController rc;
	
	public TankFactory(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		spawnUnit(RobotType.TANK);
		rc.yield();
	}

}
