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
		turnsMoved = 0;
		dir = getRandomDirection();
	}
	
	
	@Override
	public void run() throws GameActionException {
		supplyChain();

		if(robotsOfTypeOnTeam(RobotType.MINERFACTORY,rc.getTeam()) < 4 && turnsMoved>=4){
			buildUnit(RobotType.MINERFACTORY);
		}
		else if(robotsOfTypeOnTeam(RobotType.HELIPAD,rc.getTeam()) <3 && turnsMoved>=6){
			buildUnit(RobotType.HELIPAD);
		}
		else if(robotsOfTypeOnTeam(RobotType.SUPPLYDEPOT, rc.getTeam()) < 3 && turnsMoved >=6){
			buildUnit(RobotType.SUPPLYDEPOT);
		}
		else if(robotsOfTypeOnTeam(RobotType.AEROSPACELAB,rc.getTeam()) < 10 && turnsMoved >=8){
			buildUnit(RobotType.AEROSPACELAB);
		}
		else if(rc.isCoreReady()){
			moveAsCloseToDirection(dir);
			turnsMoved++;
		}
		rc.yield();

	}
	
	private void supplyChain() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<200){
				int toSupply = 0;
				toSupply = (int) (Math.abs((rc.getSupplyLevel()-ri.supplyLevel))/2);
				if(ri.type == RobotType.HQ){
					toSupply = 0;
				}
				if(rc.senseRobotAtLocation(ri.location) != null){
					if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){
						rc.transferSupplies(toSupply, ri.location);
					}
				}
			}
			
		}
	}

}
