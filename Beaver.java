package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Beaver extends BaseRobot {
	public RobotController rc;
	
	public Beaver(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		if(robotsOfTypeOnTeam(RobotType.MINERFACTORY,rc.getTeam()) < 4){
			buildUnit(RobotType.MINERFACTORY);
		}
		moveAsCloseToDirection(rc.getLocation().directionTo(ComSystem.getLocation(10)));
		rc.yield();
	}

}
