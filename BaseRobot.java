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
	
	//public void moveAsClose
	public boolean safeToMove(Direction dir){
		boolean safe = true; 
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		for(RobotInfo ri: enemiesInRange){
			if(rc.getLocation().add(dir).distanceSquaredTo(ri.location)<=ri.type.attackRadiusSquared){
				safe = false;
				break;
			}
		}
		return safe;
	}
}
