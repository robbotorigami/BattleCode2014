package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Miner extends BaseRobot {
	public RobotController rc;
	public Direction lastMove;
	public Direction defaultMove;
	public MapLocation defaultLocation;
	public final int MININGAVGCHANNEL = 50;
	public int roundLastSkipped = 0;
	
	public Miner(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		shootWeakest();
		mineAndMove();
		ComSystem.logMiningIfBetter(getOreNear(), rc.getLocation());
		//updateMiningInfo();
		rc.yield();

	}
	
	private void updateMiningInfo() throws GameActionException {
		if(lastMove != null)
			ComSystem.addToAverage(MININGAVGCHANNEL, getIndexOfDirection(lastMove));
		defaultMove = Direction.values()[ComSystem.getAverage(MININGAVGCHANNEL)];
	}	

	//Handles basic mining action
	public void mineAndMove() throws GameActionException{
		//If there is ore, and we aren't blocking miners, mine it!
		boolean skipSquare = false;
		if(ComSystem.getUselessMiners()>5){
			if(Clock.getRoundNum()-roundLastSkipped > 1){
				skipSquare = true;
				roundLastSkipped = Clock.getRoundNum();
			}
		}
		if(rc.senseOre(rc.getLocation()) >0.2 && !skipSquare){
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
				//also report that we don't have ore where we are at
				ComSystem.reportUselessMiner();
				if(rand.nextDouble()<0.5){
					//selected = rc.getLocation().directionTo(ComSystem.getMiningLoc());
					//selected = rc.getLocation().directionTo(rc.senseHQLocation()).opposite();
					selected = rc.getLocation().directionTo(ourHQ.add(ourHQ.directionTo(theirHQ),20));
				}
			}
			
			//Move to the selected square
			if(rc.isCoreReady()&&rc.canMove(selected)){
				basicPathing(selected);
				lastMove = selected;
			}
			
		}
	}

}
