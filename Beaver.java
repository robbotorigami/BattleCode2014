package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Beaver extends BaseRobot {
	public RobotController rc;
	public int turnsMoved;
	public Direction dir;
	public Beaver(RobotController rcin){
		super(rcin);
		rc = rcin;
		int turnsMoved = 0;
		dir = getRandomDirection();
	}
	
	
	@Override
	public void run() throws GameActionException {

		if(robotsOfTypeOnTeam(RobotType.MINERFACTORY,rc.getTeam()) < 4 && turnsMoved>=4){
			buildUnit(RobotType.MINERFACTORY);
		}
		else if(robotsOfTypeOnTeam(RobotType.HELIPAD,rc.getTeam()) <1 && turnsMoved>=6){
			buildUnit(RobotType.HELIPAD);
		}
		else if(robotsOfTypeOnTeam(RobotType.AEROSPACELAB,rc.getTeam()) < 1 && turnsMoved >=8){
			buildUnit(RobotType.AEROSPACELAB);
		}
		else if(rc.isCoreReady()){
			moveAsCloseToDirection(dir);
			turnsMoved++;
		}
		rc.yield();

	}

}
