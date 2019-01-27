package engine.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import demo.Game;
import engine.utilities.Vec2i;

public class AStar {
	private ArrayList<Anode> openSet = new ArrayList<>();
	private ArrayList<Anode> closedSet = new ArrayList<>();
	private HashMap <Integer, Anode> cameFrom = new HashMap<>();
	private HashMap <Float, Anode> gScore = new HashMap<>(); //gScore -> distance between start Node and this ANode
	private float M_SQRT2 = (float) Math.sqrt(2.0);
	public static final int MAXSTEPS = 2000;
	private int widthSize = (128/Game.GRAPH_DIVISOR) +1, heightSize = (20/Game.GRAPH_DIVISOR)+1;
	private boolean obstacleMap[][];
	
	public ArrayList<Anode> calculatePath(Vec2i start, Vec2i end, boolean obstacleMap[][]){
		Anode startNode = new Anode();
		Anode endNode	= new Anode();
		Anode current;
		this.obstacleMap = obstacleMap;
		
		startNode.pos 	= start;
		endNode.pos 	= end;
		
		startNode.setgScore(0);
		startNode.setfScore( heuristic(startNode, endNode)  );
		
		openSet.add(startNode);
		
		int k=0;
		while(!openSet.isEmpty()) {
			if (k++ > MAXSTEPS) 
				return null;
			
			Collections.sort(openSet);
			
			current = openSet.get(0);
			
			if(current.pos.compareTo(endNode.pos) == 0) {
				return constructPath(current);
			}
			
			openSet.remove(current);
			closedSet.add(current);
			
			for(Anode neighbor: getNeighbors(current)) {
				if(isSizeOccupied(neighbor)) {
					closedSet.add(neighbor);
				}
				
				//ignore the neighbor which is already evaluated
				if(closedSet.contains(neighbor))
					continue;
				
				//discover new node
				if(!openSet.contains(neighbor))
					openSet.add(neighbor);
						
				float gScore = current.getgScore() + distance(current,neighbor);
				
				// not a better path
				if(gScore >= neighbor.getgScore())
					continue; 
				
				neighbor.antecessor = current;
				neighbor.setgScore( gScore );
				neighbor.setfScore( neighbor.getgScore() + heuristic(neighbor, endNode) );
			}
		}
		
		
		return null;
	}
	
	public boolean isOutOfBounds(Anode node, boolean obstacleMap[][]) {
		if(node.pos.x>obstacleMap.length-1 || node.pos.y>obstacleMap[0].length-1 || node.pos.x<0 || node.pos.y<0)
			return true;
		return false;
	}
	
	private boolean isOccupied(Anode a) {
		return (!isOutOfBounds(a, obstacleMap) && obstacleMap[a.pos.x][a.pos.y]);
	}
	
	private boolean isSizeOccupied(Anode u) {
		Anode tempState = new Anode(u.pos.x , u.pos.y);
		for(int y=0; y<heightSize; y++) {
			for(int x=0; x<widthSize; x++) {
				tempState.pos.x = u.pos.x + x;
				tempState.pos.y = u.pos.y + y;
				
				if(isOccupied(tempState)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private ArrayList<Anode> getNeighbors(Anode current){
		ArrayList<Anode> neighbors = new ArrayList<>();
		Anode temp;
		
		temp = new Anode(current.pos.x + 1, current.pos.y);
		neighbors.add(temp);
		
		temp = new Anode(current.pos.x + 1, current.pos.y + 1);
		neighbors.add(temp);
		
		temp = new Anode(current.pos.x , current.pos.y + 1);
		neighbors.add(temp);
		
		temp = new Anode(current.pos.x - 1, current.pos.y + 1);
		neighbors.add(temp);
		
		temp = new Anode(current.pos.x - 1, current.pos.y);
		neighbors.add(temp);
		
		temp = new Anode(current.pos.x - 1, current.pos.y - 1);
		neighbors.add(temp);
		
		temp = new Anode(current.pos.x , current.pos.y - 1);
		neighbors.add(temp);
		
		temp = new Anode(current.pos.x + 1, current.pos.y - 1);
		neighbors.add(temp);

		return neighbors;
	}
	
	private ArrayList<Anode> constructPath(Anode current){
		Anode temp = current;
		ArrayList<Anode> path = new ArrayList<>();
		
		while(temp.antecessor!=null) {
			path.add(temp.antecessor);
			temp = temp.antecessor;
		}
		
		return path;
	}
	
	private float heuristic(Anode start, Anode end) {
		float temp;
		float min = Math.abs(start.pos.x - end.pos.x);
		float max = Math.abs(start.pos.y - end.pos.y);
		
		if (min > max){
			temp = min;
			min = max;
			max = temp;
		}
		
		return ((M_SQRT2-1.0f)*min + max);
	}
	
	private float distance(Anode a, Anode b){
		float x = a.pos.x-b.pos.x;
		float y = a.pos.y-b.pos.y;
		return (float) Math.sqrt(x*x + y*y);
	}
	
	private Anode lowestFScore(ArrayList<Anode> list) {
		Anode current = list.get(0);
		
		for(Anode a: list) {
			if(current.getfScore()>a.getfScore())
				current = a;
		}
		
		return current;
	}

	public ArrayList<Anode> getClosedSet() {
		return closedSet;
	}
}
