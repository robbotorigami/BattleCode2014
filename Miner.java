package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Miner extends BaseRobot {
	public RobotController rc;
	public Direction lastMove;
	public Direction defaultMove;
	public MapLocation defaultLocation;
	public final int MININGAVGCHANNEL = 50;
	
	public Miner(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		mineAndMove();
		updateMiningInfo();
		ComSystem.logMiningIfBetter(getOreNear(), rc.getLocation());
		rc.yield();

	}
	
	private void updateMiningInfo() throws GameActionException {
		if(lastMove != null)
			ComSystem.addToAverage(MININGAVGCHANNEL, getIndexOfDirection(lastMove));
		defaultMove = Direction.values()[ComSystem.getAverage(MININGAVGCHANNEL)];
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
			//If all ore < 0.4, follow the crowd
			if(bestOre <0.4){
				if(rand.nextDouble()<0.5){
					selected = rc.getLocation().directionTo(ComSystem.getMiningLoc());
				}
			}
			//Move to the selected square
			if(rc.isCoreReady()&&rc.canMove(selected)){
				rc.move(selected);
				lastMove = selected;
			}
			
		}
	}

}
