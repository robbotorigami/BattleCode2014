package team079;

import team079.util.ComSystem;
import battlecode.common.*;

public class HQ extends BaseRobot {
	public RobotController rc;
	public int lastTime;
	public MapLocation[] waypoints;
	public int currentWaypoint;
	public final int WAYPOINTMAXDISTANCE = 10;
	public final int SWARMCHANNEL = 199;
	public final int SWARMDISTANCE = 10;
	public int SWARMAMOUNT = 10;
	public final int SWARMOVERLOAD = 20;
	int Profiler;

	public HQ(RobotController rcin){
		super(rcin);
		rc = rcin;
		try {
			initSwarm();
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() throws GameActionException {
		Profiler = Clock.getBytecodesLeft();
		ComSystem.clearUselessMiners();
		ComSystem.clearMiningInfo();
		System.out.print((Clock.getBytecodesLeft() - Profiler) + ", ");
		handleSwarm();
		supplyChain();
		shootWeakest();
		if(robotsOfTypeOnTeam(RobotType.BEAVER,rc.getTeam()) < 3){
			spawnUnit(RobotType.BEAVER);
		}
		if(robotsOfTypeOnTeam(RobotType.BEAVER,rc.getTeam()) < 10 && rc.getTeamOre() >600){
			spawnUnit(RobotType.BEAVER);
		}
		rc.yield();
		
		if(rc.getHealth() < 50){
			rc.setTeamMemory(0, 1);
		}
		
	}
	
	public void initSwarm() throws GameActionException{
		int actWaypointDis = (int) Math.min(Math.sqrt(rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()))/2,WAYPOINTMAXDISTANCE);
		SWARMAMOUNT += Math.sqrt(ourHQ.distanceSquaredTo(theirHQ))/7;
		if(rc.getTeamMemory()[0] == 1)
			SWARMAMOUNT = 10;
		MapLocation[] towersRank = rankTowers();
		int numWaypoints = 2;
		if(towersRank.length > 4){
			numWaypoints += towersRank.length - 4;
		}
		int red = 255;
		for(int i = 0; i < towersRank.length; i++){
			rc.setIndicatorDot(towersRank[i].add(Direction.NORTH), red, 255-red, 0);
			red -= 50;
		}
		waypoints = new MapLocation[numWaypoints];
		waypoints[0] = rc.getLocation().add(rc.getLocation().directionTo(rc.senseEnemyHQLocation()), actWaypointDis);
		int i = 1;
		for(MapLocation loc:towersRank){
			if(towersRank.length-i <4){
				break;
			}
			waypoints[i] = loc;
			i++;
		}
		waypoints[waypoints.length -1] = rc.senseEnemyHQLocation();
		currentWaypoint = 0;
	}

	public void handleSwarm() throws GameActionException{
		ComSystem.clearSync(57575);

		int numTanks = robotsOfTypeOnTeam(RobotType.TANK, rc.getTeam());
		
		if(numTanks>SWARMAMOUNT){
			if(robotsAtWaypoint() && currentWaypoint < waypoints.length-1){
				currentWaypoint++;
			}
		}
		if((numTanks> SWARMOVERLOAD || Clock.getRoundNum()>1200) && currentWaypoint <1){
			currentWaypoint = 1;
		}
		if(Clock.getRoundNum() > rc.getRoundLimit() - 400){
			currentWaypoint = waypoints.length-1;
		}
		if(currentWaypoint == 0){
			ComSystem.sendLocation(199, waypoints[currentWaypoint], true);
		}else{
			ComSystem.sendLocation(199, waypoints[currentWaypoint], false);
		}
		//ComSystem.sendLocation(198, center.add(center.directionTo(waypoints[currentWaypoint]), 5), false);

	}

	public boolean robotsAtWaypoint(){
		int sumInRange = 0;
		int sumLocx = 0;
		int sumLocy = 0;
		int total = 0;
		Profiler = Clock.getBytecodesLeft();
		RobotInfo[] robots = robotsOnTeam(RobotType.TANK, rc.getTeam()); 
		System.out.println(Clock.getBytecodesLeft() - Profiler);
		for(RobotInfo ri: robots){
			if(ri == null) break;
			sumLocx+=ri.location.x;
			sumLocy+=ri.location.y;
			if(ri.location.distanceSquaredTo(waypoints[currentWaypoint]) < SWARMDISTANCE){
				sumInRange++;
			}
			total++;
		}
		MapLocation center = new MapLocation(sumLocx/total, sumLocy/total);
		if(sumInRange> 0.7*SWARMAMOUNT){
			return true;
		}
		return false;
	}
	
	private void supplyChain() throws GameActionException{
		RobotInfo[] Robots = rc.senseNearbyRobots(15, rc.getTeam());
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<200 && ri.type == RobotType.DRONE){
				int toSupply = 0;
				toSupply = (int)rc.getSupplyLevel()/2;
				if(rc.senseRobotAtLocation(ri.location) != null){
					if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){
						rc.transferSupplies(toSupply, ri.location);
					}
				}
			}
		}
		for(RobotInfo ri: Robots){
			if(ri.supplyLevel<200){
				int toSupply = 0;
				toSupply = Math.max((int) (Math.abs((rc.getSupplyLevel()-ri.supplyLevel)/16)), 200);
				if(rc.senseRobotAtLocation(ri.location) != null){
					if(rc.senseRobotAtLocation(ri.location).team == rc.getTeam()){
						rc.transferSupplies(toSupply, ri.location);
						return;
					}
				}
			}
		}
	}
	
	public class TowerInfo{
		MapLocation loc;
		int distance;
		@Override 
		public String toString(){
			return loc.toString() + "/" +distance;
		}
	}
	
	public MapLocation[] rankTowers(){
		MapLocation[] TowerLocs = rc.senseEnemyTowerLocations();
		TowerInfo[] towers = new TowerInfo[TowerLocs.length];
		for(int i =0; i < TowerLocs.length; i++){
			towers[i] = new TowerInfo();
			towers[i].loc = TowerLocs[i];	
			towers[i].distance = calcDistanceFromCenterLine(TowerLocs[i]);
		}
		boolean swapsMade = true;
		while(swapsMade){
			swapsMade = false;
			for(int i = 0; i< towers.length - 1; i++){
				//System.out.print(towers[i] + ",");
				if(towers[i].distance > towers[i+1].distance){
					TowerInfo temp = towers[i];
					towers[i] = towers[i+1];
					towers[i+1] = temp;
					swapsMade = true;
				}
			}
			//System.out.println();
		}
		MapLocation[] rankedLocs = new MapLocation[TowerLocs.length];
		for(int i = 0; i< towers.length; i++){
			rankedLocs[i] = towers[i].loc;
		}
		return rankedLocs;
	}
	
	public int calcDistanceFromCenterLine(MapLocation toCheck){
		boolean vertical = false;
		int dx = ourHQ.x - theirHQ.x;
		int dy = ourHQ.y - theirHQ.y;
		double slope = 1;
		if(dx != 0)
			slope = dy/dx;
		else
			vertical = true;
		double lineAngle = Math.toDegrees(Math.atan2(dy, dx));
		if(lineAngle >180) lineAngle -=360;
		dx = ourHQ.x - toCheck.x;
		dy = ourHQ.y - toCheck.y;
		double angle = Math.toDegrees(Math.atan2(dy, dx));
		if(angle >180) angle -= 360;
		
		boolean toTheRight;
		if(lineAngle > 0){
			if(angle > lineAngle -180 && angle < lineAngle){
				toTheRight = true;
			}else{
				toTheRight = false;
			}
		}else{
			if(angle > lineAngle && angle < lineAngle + 180){
				toTheRight = false;
			}else{
				toTheRight = true;
			}
		}
		
		Direction perpendicular;
		if(toTheRight){
			perpendicular = ourHQ.directionTo(theirHQ).rotateRight().rotateRight();
		}else{
			perpendicular = ourHQ.directionTo(theirHQ).rotateLeft().rotateLeft();
		}
		
		int distance = 0;
		MapLocation orig = toCheck;
		while((Math.abs((toCheck.x-ourHQ.x)*slope - (toCheck.y - ourHQ.y)) > 1 && !vertical)|| toCheck.x - ourHQ.x == 0 && vertical){
			distance++;
			toCheck = toCheck.add(perpendicular);
			//System.out.println(perpendicular + ", " + toCheck+ "," + orig);
			if(distance > 120){
				return 1000000000;
			}
		}
		
		//System.out.println(toTheRight + ", " + distance + ", " + toCheck);
		return distance;
		
	}

}