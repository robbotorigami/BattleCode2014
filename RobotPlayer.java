package team079;

import battlecode.common.*;

public class RobotPlayer {
	public static BaseRobot myself;
	public static void run(RobotController rc){
		//Initialize myself to a type based off of what type of robot we are
		switch(rc.getType()){
		case HQ:
			myself = new HQ(rc);
			break;
		case BEAVER:
			myself = new Beaver(rc);
			break;
		case SUPPLYDEPOT:
			myself = new SupplyDepot(rc);
			break;
		case TECHNOLOGYINSTITUTE:
			myself = new TechnologyInstitute(rc);
			break;
		case COMPUTER:
			myself = new Computer(rc);
			break;
		case TRAININGFIELD:
			myself = new TrainingField(rc);
			break;
		case COMMANDER:
			myself = new Commander(rc);
			break;
		case BARRACKS:
			myself = new Barracks(rc);
			break;
		case SOLDIER:
			myself = new Soldier(rc);
			break;
		case BASHER:
			myself = new Basher(rc);
			break;
		case TANKFACTORY:
			myself = new TankFactory(rc);
			break;
		case TANK:
			myself = new Tank(rc);
			break;
		case HELIPAD:
			myself = new Helipad(rc);
			break;
		case DRONE:
			myself = new Drone(rc);
			break;
		case AEROSPACELAB:
			myself = new AerospaceLab(rc);
			break;
		case LAUNCHER:
			myself = new Launcher(rc);
			break;
		case HANDWASHSTATION:
			myself = new SuperImportant(rc);
			break;
		case MINERFACTORY:
			myself = new MinerFactory(rc);
			break;
		case MINER:		
			myself = new Miner(rc);
			break;
		case TOWER:
			myself = new Tower(rc);
			break;
		case MISSILE:
			myself = new Missile(rc);
			break;
		}
		//While true, run the run method in myself
		while(true){
			try{
				myself.run();
			}catch(GameActionException e){
				e.printStackTrace();
			}
		}
	}

}
