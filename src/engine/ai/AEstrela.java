package engine.ai;

import java.util.ArrayList;
import java.util.HashMap;

import engine.utilities.Vec2i;

public class AEstrela {
	private ArrayList<Anode> openSet = new ArrayList<>();
	private ArrayList<Anode> closedSet = new ArrayList<>();
	private HashMap <Integer, Anode> cameFrom = new HashMap<>();
	private HashMap <Float, Anode> gScore = new HashMap<>(); //gScore -> distance between start Node and this ANode
	private float M_SQRT2 = (float) Math.sqrt(2.0);
	
	
	public ArrayList<Anode> calculatePath(Vec2i start, Vec2i end, boolean obstacleMap[][]){
		Anode startNode = new Anode();
		Anode endNode	= new Anode();
		Anode current;
		
		startNode.pos 	= start;
		endNode.pos 	= end;
		
		
		startNode.setgScore(0);
		startNode.setfScore( heuristic(startNode, endNode)  );
		
		openSet.add(startNode);
		
		while(!openSet.isEmpty()) {
			current = lowestFScore(openSet);
			
			if(current.pos.compareTo(endNode.pos) == 0) 
				return constructPath(current);
			
			openSet.remove(current);
			closedSet.add(current);
			
			for(Anode neighbor: getNeighbors(current)) {
				if(closedSet.contains(neighbor))
					continue; //ignore the neighbor which is already evaluated
				
				if(!openSet.contains(neighbor)) //discover new node
					openSet.add(neighbor);
				
				float gScore = current.getgScore() + distance(current,neighbor);
				if(gScore >= neighbor.getgScore())
					continue; // not a better path
				
				neighbor.cameFrom = current;
				neighbor.setgScore( gScore );
				neighbor.setfScore( neighbor.getgScore() + heuristic(neighbor, endNode) );
			}
		}
		
		return null;
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
		
		while(temp.cameFrom!=null) {
			path.add(temp.cameFrom);
			temp = temp.cameFrom;
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
}
