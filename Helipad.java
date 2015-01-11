package team079;

import battlecode.common.*;

public class Helipad extends BaseRobot {
	public RobotController rc;
	public Helipad(RobotController rcin){
		super(rcin);
		rc = rcin;
		
	}
	
	@Override
	public void run() throws GameActionException {
		if(robotsOfTypeOnTeam(RobotType.DRONE, rc.getTeam())<5){
		spawnUnit(RobotType.DRONE);
		
		}
	}

}
