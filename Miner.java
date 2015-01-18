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
		supplyChain();
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
			if(Clock.getRoundNum()-roundLastSkipped > 50){
				skipSquare = true;
				roundLastSkipped = Clock.getRoundNum();
			}
		}
		if(rc.senseOre(rc.getLocation()) >0.2 && !skipSquare){
			if(rc.isCoreReady()&&rc.canMine())
				rc.mine();
		}else{
			//also report that we don't have ore where we are at
			ComSystem.reportUselessMiner();
			//If there isn't ore, find it!
			MapLocation[] toTry = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 2);
			double bestOre = -1;
			Direction selected = null;
			//Find the best square near us
			for(MapLocation loc:toTry){
				if(rc.senseOre(loc)> bestOre){
					bestOre = rc.senseOre(loc);
					selected = rc.getLocation().directionTo(loc);
				}
			}			
			//If all ore < 0.4, follow the crowd
			if(bestOre <0.4){
				
				if(rand.nextDouble()<0.0){
					selected = rc.getLocation().directionTo(ComSystem.getMiningLoc());
					//selected = rc.getLocation().directionTo(rc.senseHQLocation()).opposite();
					//selected = rc.getLocation().directionTo(ourHQ.add(ourHQ.directionTo(theirHQ),20));
				}else{
					if(rc.getID()%2 == 0)
						selected = rc.getLocation().directionTo(ourHQ.add(ourHQ.directionTo(theirHQ).rotateLeft().rotateLeft(),20));
					else
						selected = rc.getLocation().directionTo(ourHQ.add(ourHQ.directionTo(theirHQ).rotateRight().rotateRight(),20));
				}
			}
			
			//Move to the selected square
			if(rc.isCoreReady()){
				if(basicPathing(selected)){
					ComSystem.logMined(rc.getLocation().add(selected));
				}
				lastMove = selected;
			}
			
		}
	}
	private void supplyChain() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<rc.getSupplyLevel()*0.75){
				int toSupply = 0;				
				if(ri.type == RobotType.MINER){
					toSupply = (int) ((rc.getSupplyLevel()-ri.supplyLevel)/2);
				}
				if(rc.senseRobotAtLocation(ri.location) != null && toSupply !=0){
					rc.transferSupplies(toSupply, ri.location);
					break;
				}
			}
			
		}
	}

}
