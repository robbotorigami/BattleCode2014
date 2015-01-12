package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Barracks extends BaseRobot {
	public RobotController rc;
	
	public Barracks(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		if(robotsOfTypeOnTeam(RobotType.SOLDIER, rc.getTeam()) < 10 || robotsOfTypeOnTeam(RobotType.LAUNCHER, rc.getTeam()) >20 )
			spawnUnit(RobotType.SOLDIER);
		rc.yield();

	}

}
