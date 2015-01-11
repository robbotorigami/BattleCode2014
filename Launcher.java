package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Launcher extends BaseRobot {
	public RobotController rc;
	//public int myID;
	
	public Launcher(RobotController rcin){
		super(rcin);
		rc = rcin;
		//int myID = robotsOfTypeOnTeam(RobotType.LAUNCHER, rc.getTeam());
	}
	
	@Override
	public void run() throws GameActionException {
		//ComSystem.sendLocation(myID+200,rc.getLocation(), true);
		launchAtWeakest();
		moveAsCloseToDirection(rc.getLocation().directionTo(ComSystem.getLocation(199)));
	}
	

}
