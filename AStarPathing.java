package team210;

import java.util.ArrayList;
import java.util.Iterator;

import team210.AStarPathing.metaLocation;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;



public class AStarPathing{
	public static MetaTerrain[][] metaMap;
	public static int metaWidth;
	public static int metaHeight;
	public static RobotController rc;
	public static int metaCellSize;
	public static MapLocation pointA;
	public static MapLocation pointB;
	public static boolean MetaMapInitialized = false;
	public static boolean PathingInitialized = false;
	public static int i; //For use in replaced for loops
	public static int j;
	
	public static boolean InitailizeIteratorInator(RobotController rcinit, int cellSize){
		if(!MetaMapInitialized){
			//Initalize robot controller 
			rc = rcinit;
			//Initalize the meta map
			metaWidth = (rc.getMapWidth()/cellSize)+2;
			metaHeight = (rc.getMapHeight()/cellSize)+2;

			System.out.println(Integer.toString(metaWidth)+Integer.toString(metaHeight));
			metaMap = new MetaTerrain[metaWidth][metaHeight];
			metaCellSize = cellSize;
			MetaMapInitialized = true;
			i = metaWidth-1;
			j = metaHeight-1;
		}
		metaMap[i][j] = evaluateMetaTile(i, j);
		j--;
		if(j<0){
			j = metaHeight-1;
			i--;
			if(i<0){
				printMap(metaMap); //DEBUG
				return true;
			}
		}
		return false;
	}
	//variables for pathing
	public static metaLocation[][] parents;
	public static int[][] G;
	public static int[][] H;
	public static metaLocation start;
	public static metaLocation end;
	public static ArrayList<metaLocation> closedList;
	public static ArrayList<metaLocation> openList;
	public static ArrayList<MapLocation> badPathing = new ArrayList<MapLocation>();
	public static int[][] adjacent = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};
	public static boolean notDone;
	
	public static ArrayList<MapLocation> returnBestPathIteratorInator(MapLocation pointAin, MapLocation pointBin){
		if(!PathingInitialized){
			PathingInitialized = true;
			badPathing.add(new MapLocation(-1,-1));
			pointA = pointAin;
			pointB = pointBin;
			System.out.println(Integer.toString(pointA.x)+","+Integer.toString(pointA.y)+"|"+Integer.toString(pointB.x)+","+Integer.toString(pointB.y));
			parents = new metaLocation[metaWidth][metaHeight];
			G = new int[metaWidth][metaHeight]; 
			H = new int[metaWidth][metaHeight];
			start = new metaLocation(pointA.x/metaCellSize+1, pointA.y/metaCellSize+1);
			end = new metaLocation(pointB.x/metaCellSize+1, pointB.y/metaCellSize+1);
			closedList = new ArrayList<metaLocation>();
			openList = new ArrayList<metaLocation>();
			openList.add(start);
			notDone = true;
			for(int i = 0; i<metaWidth; i++){
				for(int j = 0; j<metaHeight; j++){
					H[i][j] = Math.abs(end.x-i)+Math.abs(end.y-j);
				}
			}
			if(start ==end){
				ArrayList<MapLocation> scaled = new ArrayList<MapLocation>();
					scaled.add(new MapLocation((start.x-1) * metaCellSize+metaCellSize/2,(start.y-1) * metaCellSize+metaCellSize/2));
				return scaled;
			}
		}
		if(notDone){
			//printPath(closedList, openList); //DEBUG
			Iterator<metaLocation> openItr = openList.iterator();
			int fToBeat = 10000000;
			metaLocation current = openList.get(0);
			while(openItr.hasNext()){
				metaLocation toCheck = (metaLocation) openItr.next();
				int fToCheck = G[toCheck.x][toCheck.y]+H[toCheck.x][toCheck.y];
				if(fToCheck<fToBeat){
					fToBeat = fToCheck;
					current = toCheck;
				}
			}
			//System.out.println("current.x"+Integer.toString(current.x)+"current.y"+Integer.toString(current.y));
			metaLocation fixer = new metaLocation(current.x, current.y);
			openList.remove(fixer);
			closedList.add(current);
			for(int i =0; i<=7; i++){
				metaLocation toTry = new metaLocation(current.x+adjacent[i][0],current.y+adjacent[i][1]);
				if(validMovement(current, toTry) && !closedList.contains(toTry)){
					if(!openList.contains(toTry)){
						openList.add(toTry);
						parents[toTry.x][toTry.y]= current;
					}else{
						int gFromCurrent = G[current.x][current.y];
						if(Math.abs(adjacent[i][0])+Math.abs(adjacent[i][1]) == 1){
							gFromCurrent+=10;
						}else{
							gFromCurrent+=14;
						}
						if(G[toTry.x][toTry.y]>gFromCurrent){
							G[toTry.x][toTry.y]=gFromCurrent;

							parents[toTry.x][toTry.y]= current;
						}
					}
				}
			}
			if(openList.contains(end))
				notDone = false;
			if(openList.isEmpty()){
				PathingInitialized = false;
				return badPathing;
			}
			
		}else{
			metaLocation current = end;
			ArrayList<metaLocation> reversed = new ArrayList<metaLocation>();
			while(true){
				if(current==start){
					break;
				}
				reversed.add(current);
				current = parents[current.x][current.y];
			}
			
			ArrayList<metaLocation> correct = new ArrayList<metaLocation>(reversed.size()); 

			for(int i=reversed.size()-1;i>=0;i--) 
				correct.add(reversed.get(i)); 
			ArrayList<MapLocation> scaled = new ArrayList<MapLocation>(correct.size());
			for(int i= 0; i<correct.size(); i++)
				scaled.add(new MapLocation((correct.get(i).x-1) * metaCellSize+metaCellSize/2,(correct.get(i).y-1) * metaCellSize+metaCellSize/2));
			PathingInitialized = false;
			return scaled;
		}
		
		return null;
		
	}
	public static ArrayList<MapLocation> returnBestPath(MapLocation pointA, MapLocation pointB){
		metaLocation[][] parents = new metaLocation[metaWidth][metaHeight];
		int[][] G = new int[metaWidth][metaHeight];
		int[][] H = new int[metaWidth][metaHeight];
		metaLocation start = new metaLocation(pointA.x/metaCellSize+1, pointA.y/metaCellSize+1);
		metaLocation end = new metaLocation(pointB.x/metaCellSize+1, pointB.y/metaCellSize+1);
		ArrayList<metaLocation> closedList = new ArrayList<metaLocation>();
		ArrayList<metaLocation> openList = new ArrayList<metaLocation>();
		openList.add(start);
		int[][] adjacent = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};
		boolean notDone = true;
		for(int i = 0; i<metaWidth; i++){
			for(int j = 0; j<metaHeight; j++){
				H[i][j] = Math.abs(end.x-i)+Math.abs(end.y-j);
			}
		}
		while(notDone){
			//printPath(closedList, openList);
			Iterator<metaLocation> openItr = openList.iterator();
			int fToBeat = 10000000;
			metaLocation current = openList.get(0);
			while(openItr.hasNext()){
				metaLocation toCheck = (metaLocation) openItr.next();
				int fToCheck = G[toCheck.x][toCheck.y]+H[toCheck.x][toCheck.y];
				if(fToCheck<fToBeat){
					fToBeat = fToCheck;
					current = toCheck;
				}
			}
			//System.out.println("current.x"+Integer.toString(current.x)+"current.y"+Integer.toString(current.y));
			metaLocation fixer = new metaLocation(current.x, current.y);
			openList.remove(fixer);
			closedList.add(current);
			for(int i =0; i<=7; i++){
				metaLocation toTry = new metaLocation(current.x+adjacent[i][0],current.y+adjacent[i][1]);
				if(validMovement(current, toTry) && !closedList.contains(toTry)){
					if(!openList.contains(toTry)){
						openList.add(toTry);
						parents[toTry.x][toTry.y]= current;
					}else{
						int gFromCurrent = G[current.x][current.y];
						if(Math.abs(adjacent[i][0])+Math.abs(adjacent[i][1]) == 1){
							gFromCurrent+=10;
						}else{
							gFromCurrent+=14;
						}
						if(G[toTry.x][toTry.y]>gFromCurrent){
							G[toTry.x][toTry.y]=gFromCurrent;

							parents[toTry.x][toTry.y]= current;
						}
					}
				}
			}
			if(openList.contains(end))
				notDone = false;
			if(openList.isEmpty())
				return null;
			
		}
		metaLocation current = end;
		ArrayList<metaLocation> reversed = new ArrayList<metaLocation>();
		while(true){
			if(current==start){
				break;
			}
			reversed.add(current);
			current = parents[current.x][current.y];
		}
		
		ArrayList<metaLocation> correct = new ArrayList<metaLocation>(reversed.size()); 

		for(int i=reversed.size()-1;i>=0;i--) 
			correct.add(reversed.get(i)); 
		ArrayList<MapLocation> scaled = new ArrayList<MapLocation>(correct.size());
		for(int i= 0; i<correct.size(); i++)
			scaled.add(new MapLocation((correct.get(i).x-1) * metaCellSize+metaCellSize/2,(correct.get(i).y-1) * metaCellSize+metaCellSize/2));
		return scaled;
	}
	public static MetaTerrain evaluateMetaTile(int x, int y){
		if(x == 0 || y == 0 || x == metaWidth-1 || y == metaHeight-1){//if we are on boundry, not transversable
			return MetaTerrain.AllBlocked;
		}
		boolean hTravelable = true;
		boolean vTravelable = true;
		boolean[] filled = new boolean[metaCellSize];
		boolean[] prevFilled = new boolean[metaCellSize];
		MetaTerrain cellType;
		//check if hTravelable
		for(int i = metaCellSize-1; i>=0; i--){
			for(int j = metaCellSize-1; j>=0; j--){
				//if terrain is not passable indicate so in array
				TerrainTile type;
				type = rc.senseTerrainTile(new MapLocation((x-1)*metaCellSize+i,(y-1)*metaCellSize+j));
				if(type == TerrainTile.OFF_MAP || type == TerrainTile.VOID)
					filled[j] = true;
			}
			//check to see if this row is passable
			boolean temp = false;
			for(int k = metaCellSize-1; k>=0; k--)
				if(!filled[k] && !prevFilled[k])
					temp = true;
			//if it is not passable then the meta cell is not
			if(!temp)
				hTravelable = false;
			prevFilled = filled;
		}
		filled = new boolean[metaCellSize];
		prevFilled = new boolean[metaCellSize];
		//check if vTravelable
		for(int j = metaCellSize-1; j>=0; j--){
			for(int i = metaCellSize-1; i>=0; i--){
				//if terrain is not passable indicate so in array
				TerrainTile type;
				type = rc.senseTerrainTile(new MapLocation((x-1)*metaCellSize+i,(y-1)*metaCellSize+j));
				if(type == TerrainTile.OFF_MAP || type == TerrainTile.VOID)
					filled[i] = true;
			}
			//check to see if this row is passable
			boolean temp = false;
			for(int k = metaCellSize-1; k>=0; k--)
				if(!filled[k] && !prevFilled[k])
					temp = true;
			//if it is not passable then the meta cell is not
			if(!temp)
				vTravelable = false;
			prevFilled = filled;
		}
		if(vTravelable == true && hTravelable == true){
			cellType = MetaTerrain.AllTraversable;
		}else if(hTravelable == true){
			cellType = MetaTerrain.VerticalBlocked;
		}else if(vTravelable == true){
			cellType = MetaTerrain.HorizontalBlocked;
		}else{
			cellType = MetaTerrain.AllBlocked;
		}
		return cellType;
		
	}

	public static void printMap(MetaTerrain[][] map){
		for(int j = 1; j<map[0].length-1; j++){
			String line = "";
			for(int i=1; i<map.length-1; i++){
				char d;
				switch(map[i][j]){
				case AllTraversable:
					d = '.';
					break;
				case VerticalBlocked:
					d = '-';
					break;
				case HorizontalBlocked:
					d='|';
					break;
				case AllBlocked:
					d='#';
					break;
				default:
					d = '#';
					break;
				}
				line+=d;
			}
			System.out.println(line);
		}
	}
	
	public static void printPath(ArrayList<metaLocation> closed, ArrayList<metaLocation> open){
		char[][] path = new char[metaWidth][metaHeight];
		for(int i =0; i < metaWidth; i++){
			for(int j = 0; j <metaHeight; j++){
				switch(metaMap[i][j]){
				case AllTraversable:
					path[i][j] = '.';
					break;
				case VerticalBlocked:
					path[i][j] = '-';
					break;
				case HorizontalBlocked:
					path[i][j]='|';
					break;
				case AllBlocked:
					path[i][j]='#';
					break;
				default:
					path[i][j] = '#';
					break;
				}
			}
		}
		for(metaLocation loc:closed){
			path[loc.x][loc.y] = 'X'; 
		}
		for(metaLocation loc:open){
			path[loc.x][loc.y] = 'O'; 
		}
		for(int j = 0; j <path[0].length;j++){
			char[] line = new char[path.length];
			for(int i = 0; i <path.length;i++){
				line[i]=path[i][j];
			}

			System.out.println(line);
		}
	}
	
	public static final class metaLocation{
		public int x;
		public int y;
		public metaLocation(int xin, int yin){
			x = xin;
			y = yin;
		}
		@Override
	    public boolean equals(Object other) {
	        if (!(other instanceof metaLocation)) {
	            return false;
	        }
	        metaLocation otherLoc = (metaLocation) other;
	        return this.x == otherLoc.x &&
	               this.y == otherLoc.y;
	    }
	}
	
	public static boolean validMovement(metaLocation pointA, metaLocation pointB){
		boolean valid = true;
		MetaTerrain terrainA = metaMap[pointA.x][pointA.y];
		MetaTerrain terrainB = metaMap[pointB.x][pointB.y];
		if(terrainA == MetaTerrain.AllBlocked || terrainB == MetaTerrain.AllBlocked /*|| terrainA == MetaTerrain.HorizontalBlocked || terrainB == MetaTerrain.HorizontalBlocked || terrainA == MetaTerrain.VerticalBlocked || terrainB == MetaTerrain.VerticalBlocked*/){
			return false;
		}
		MetaTerrain terrainAU = metaMap[pointA.x][pointA.y+1];
		MetaTerrain terrainAD = metaMap[pointA.x][pointA.y-1];
		MetaTerrain terrainAL = metaMap[pointA.x-1][pointA.y];
		MetaTerrain terrainAR = metaMap[pointA.x+1][pointA.y];
		int difx = pointB.x-pointA.x;
		int dify = pointB.y-pointA.y;
		
		if(difx == 1 && dify == 0){//moving to the east, both need to be horizontally transversable
			if(terrainA == MetaTerrain.HorizontalBlocked || terrainB == MetaTerrain.HorizontalBlocked){
				return false;
			}else{
				return true;
			}
		}
		if(difx == 1 && dify == 1){//north-east. Either need to be able to do vert-all-hor or hor-all-vert
			if(!(terrainA != MetaTerrain.VerticalBlocked && terrainAU == MetaTerrain.AllTraversable && terrainB!= MetaTerrain.HorizontalBlocked) && !(terrainA != MetaTerrain.HorizontalBlocked && terrainAR == MetaTerrain.AllTraversable && terrainB!= MetaTerrain.VerticalBlocked)){
				return false;
			}else{
				return true;
			}
		}
		if(difx == 0 && dify == 1){//moving north, both need to be vertically transversable
			if((terrainA == MetaTerrain.VerticalBlocked || terrainB == MetaTerrain.VerticalBlocked)){
				return false;
			}else{
				return true;
			}
		}
		if(difx == -1 && dify == 1){//north-west. Either need to be able to do vert-all-hor or hor-all-vert
			if(!(terrainA != MetaTerrain.VerticalBlocked && terrainAU == MetaTerrain.AllTraversable && terrainB!= MetaTerrain.HorizontalBlocked) && !(terrainA != MetaTerrain.HorizontalBlocked && terrainAL == MetaTerrain.AllTraversable && terrainB!= MetaTerrain.VerticalBlocked)){
				return false;
			}else{
				return true;
			}
		}
		if(difx == -1 && dify == 0){//moving to the west, both need to be horizontally transversable
			if(terrainA == MetaTerrain.HorizontalBlocked || terrainB == MetaTerrain.HorizontalBlocked){
				return false;
			}else{
				return true;
			}
		}
		if(difx == -1 && dify == -1){//south-west. Either need to be able to do vert-all-hor or hor-all-vert
			if(!(terrainA != MetaTerrain.VerticalBlocked && terrainAD == MetaTerrain.AllTraversable && terrainB!= MetaTerrain.HorizontalBlocked) && !(terrainA != MetaTerrain.HorizontalBlocked && terrainAL == MetaTerrain.AllTraversable && terrainB!= MetaTerrain.VerticalBlocked)){
				return false;
			}else{
				return true;
			}
		}
		if(difx == 0 && dify == -1){//moving south, both need to be vertically transversable
			if((terrainA == MetaTerrain.VerticalBlocked || terrainB == MetaTerrain.VerticalBlocked)){
				return false;
			}else{
				return true;
			}
		}
		if(difx == 1 && dify == -1){//south-east. Either need to be able to do vert-all-hor or hor-all-vert
			if(!(terrainA != MetaTerrain.VerticalBlocked && terrainAD == MetaTerrain.AllTraversable && terrainB!= MetaTerrain.HorizontalBlocked) && !(terrainA != MetaTerrain.HorizontalBlocked && terrainAR == MetaTerrain.AllTraversable && terrainB!= MetaTerrain.VerticalBlocked)){
				return false;
			}else{
				return true;
			}
		}
		//if we get here something is wrong
		return false;
	}
}