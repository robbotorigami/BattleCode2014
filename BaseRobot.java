package testplayer2;

import java.util.Random;
import battlecode.common.*;

public abstract class BaseRobot {
	public RobotController rc;
	public Random rand;
	public Direction facing = Direction.NORTH;
	public BaseRobot(RobotController rcin){
		rc = rcin;
		
		rand= new Random(rc.getID());
	}
	
	//Abstract method for major functionality
	public abstract void run() throws GameActionException;
	public Direction getRandomDirection(){
		return Direction.values()[rand.nextInt(8)];
	}
}
