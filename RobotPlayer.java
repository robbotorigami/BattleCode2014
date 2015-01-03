package team210;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class RobotPlayer{
	public static RobotController rc;
	public static MapLocation noiseTower;
	public static boolean sweep;
	public static int lastRobotCount = 0;
	
	public static void run(RobotController rcIn){
		rc = rcIn;
		if(rc.getType() == RobotType.HQ)
			HQ.runHeadQuarters(rc);
		if(rc.getType() == RobotType.PASTR)
			runPastr();
		if(rc.getType() == RobotType.NOISETOWER)
			runNoiseTower();
		if(rc.getType() == RobotType.SOLDIER)
			Soldier.run(rc);
		
	}
	private static void runNoiseTower() {
		int input = 0;
		try {
			input = rc.readBroadcast(303);
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(input/10000==1){
			sweep = true;
		}else{
			sweep = false;
		}
		input -=10000;
		noiseTower = new MapLocation(input/100,input%100);
		if(sweep){
			Direction CurDir = Direction.WEST;
			int dis = 11;
			while(true){
				if(rc.isActive()&&rc.canAttackSquare(noiseTower.add(CurDir,dis))){
					try {
						if(rc.senseTerrainTile(noiseTower.add(CurDir,dis))!= TerrainTile.VOID && rc.senseTerrainTile(noiseTower.add(CurDir,dis))!= TerrainTile.OFF_MAP ){
							if(dis>5){
								rc.attackSquare(noiseTower.add(CurDir,dis));
							}else{
								rc.attackSquareLight(noiseTower.add(CurDir,dis));
							}
						}
						if(dis>5){
							CurDir = CurDir.rotateLeft();
						}else{
							CurDir = CurDir.rotateLeft().rotateLeft();
						}
						if(CurDir==Direction.WEST){
							dis--;
						}
						if(dis<=2){
							dis = 11;
						}

					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(rc.senseRobotCount()< lastRobotCount){
					try {
						rc.broadcast(9,1);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				lastRobotCount = rc.senseRobotCount();
				rc.yield();
			}
		}
		
	}
	private static void runPastr() {
		while(true){
			if(rc.senseRobotCount()< lastRobotCount){
				try {
					rc.broadcast(1,1);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			lastRobotCount = rc.senseRobotCount();
			rc.yield();
		}
		
	}
}