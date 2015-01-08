package team079;

import battlecode.common.*;

public class Miner extends BaseRobot {
	public RobotController rc;
	
	public Miner(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		mineAndMove();
		rc.yield();

	}
	
	//Handles basic mining action
	public void mineAndMove() throws GameActionException{
		//If there is ore, mine it!
		if(rc.senseOre(rc.getLocation()) >0.2){
			if(rc.isCoreReady()&&rc.canMine())
				rc.mine();
		}else{
			//If there isn't ore, find it!
			Direction[] toTry = Direction.values();
			double bestOre = -1;
			Direction selected = null;
			//Find the best square near us
			for(Direction dir:toTry){
				if(rc.senseOre(rc.getLocation().add(dir))> bestOre && rc.canMove(dir)){
					bestOre = rc.senseOre(rc.getLocation().add(dir));
					selected = dir;
				}
			}
			//Move to the selected square
			if(rc.isCoreReady()&&rc.canMove(selected)){
				rc.move(selected);
			}
			
		}
	}

}
