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
		if(robotsOfTypeOnTeam(RobotType.DRONE, rc.getTeam()) < 3){
			spawnUnit(RobotType.DRONE);

		} else if (robotsOfTypeOnTeam(RobotType.MINER, rc.getTeam()) > 5 && robotsOfTypeOnTeam(RobotType.DRONE, rc.getTeam()) < 6){
			spawnUnit(RobotType.DRONE);
		} else if (robotsOfTypeOnTeam(RobotType.DRONE, rc.getTeam().opponent()) > 1 && Clock.getRoundNum() < 1000){
			spawnUnit(RobotType.DRONE);
		}
	}

}
