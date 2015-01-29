package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class Barracks extends BaseRobot {
	public RobotController rc;
	public boolean towerDefense;
	
	public Barracks(RobotController rcin){
		super(rcin);
		rc = rcin;
		towerDefense = false;
		for(MapLocation loc: rc.senseTowerLocations()){
			if(rc.getLocation().isAdjacentTo(loc)){
				towerDefense = true;
			}
		}
	}
	
	@Override
	public void run() throws GameActionException {
		if(towerDefense){
			towerDefense();
		}else{
			opsAsNormal();
		}
		rc.yield();

	}
	
	public void opsAsNormal() throws GameActionException{
		if(robotsOfTypeOnTeam(RobotType.SOLDIER, rc.getTeam()) < 10 || robotsOfTypeOnTeam(RobotType.TANK, rc.getTeam()) >40 )
			spawnUnit(RobotType.SOLDIER);
	}
	
	public void towerDefense() throws GameActionException{
		int limit = (rc.senseNearbyRobots(40, rc.getTeam().opponent()).length > 0)? 16:6;
		if(robotsOnTeam(RobotType.BASHER, 40, rc.getTeam()).length + robotsOnTeam(RobotType.SOLDIER, 40, rc.getTeam()).length < limit){
			if(robotsOnTeam(RobotType.SOLDIER, rc.getTeam()).length + robotsOnTeam(RobotType.BASHER, rc.getTeam()).length < 30){
				if(rand.nextDouble() < 0.1){
					spawnUnit(RobotType.BASHER);
				}else{
					spawnUnit(RobotType.SOLDIER);
				}
			}
		}
	}

}
