package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class MinerFactory extends BaseRobot {
	public RobotController rc;
	
	public MinerFactory(RobotController rcin){
		super(rcin);
		rc = rcin;
	}
	
	@Override
	public void run() throws GameActionException {
		if(ComSystem.getUselessMiners() < 10 && robotsOfTypeOnTeam(RobotType.MINER, rc.getTeam()) < 30){
			spawnUnit(RobotType.MINER);
		}
		rc.yield();
	}

}
