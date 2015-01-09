package testplayer2;

import java.util.Random;

import battlecode.common.*;

public abstract class BaseRobot {
	public RobotController rc;
	public int numberOfBeavers;
	public Random randall; 
	public Direction dir; 
	public BaseRobot(RobotController rcin){
		rc = rcin;
	
	
	}
	
	//Abstract method for major functionality
	public abstract void run() throws GameActionException;
	
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
	public Direction getRandomDirection(){
		return Direction.values()[randall.nextInt(8)];
	}
	
	
}
