package team079;

import java.util.Random;

import battlecode.common.*;

public abstract class BaseRobot {
	public RobotController rc;
	public Random rand; //Random number generator
	public MapLocation theirHQ; //location of their HQ
	public MapLocation ourHQ; //location of our HQ
	
	public BaseRobot(RobotController rcin){
		rc = rcin;
		rand = new Random(rc.getID());
		ComSystem.init(rc);
		ourHQ = rc.senseHQLocation();
		theirHQ = rc.senseEnemyHQLocation();
	}
	
	//Abstract method for major functionality
	public abstract void run() throws GameActionException;
	
	//-------------------Base Methods for all Units------------------
	
	//Will try to spawn a Unit of a given type
	public void spawnUnit(RobotType toSpawn) throws GameActionException{
		if(rc.isCoreReady()){
			boolean unitSpawned = false;
			while(unitSpawned != true){
				//Have a list of all directions biased towards their HQ. try and spawn for all in list
				Direction dir = ourHQ.directionTo(theirHQ);
				Direction[] toTry = {dir,
						dir.rotateLeft(),
						dir.rotateRight(),
						dir.rotateLeft().rotateLeft(),
						dir.rotateRight().rotateRight(),
						dir.rotateLeft().rotateLeft().rotateLeft(),
						dir.rotateRight().rotateRight().rotateRight(),
						dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
				};
				for(Direction buildDir: toTry){
					if(rc.isCoreReady()&&rc.canSpawn(Direction.NORTH, toSpawn)){					
						rc.spawn(Direction.NORTH, toSpawn);
						unitSpawned = true;
						break;
					}
				}
			}
		}

	}
	
	//Move as close as possible to the provided direction
	public void moveAsCloseToDirection(Direction toMove) throws GameActionException{
		if(rc.isCoreReady()){
			Direction[] toTry = {toMove,
					toMove.rotateLeft(),
					toMove.rotateRight(),
					toMove.rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight(),
					toMove.rotateLeft().rotateLeft().rotateLeft(),
					toMove.rotateRight().rotateRight().rotateRight(),
					toMove.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
			};
			for(Direction dir:toTry){
				if(rc.canMove(dir)&&rc.isCoreReady()){
					rc.move(dir);
				}
			}
		}		
	}
	
	//Returns a random direction	
	public Direction getRandomDirection(){
		return Direction.values()[rand.nextInt(8)];
	}
}
