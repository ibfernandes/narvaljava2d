package engine.ai;

import java.util.ArrayList;

import engine.logic.GameObject;
import gameStates.Game;

public class ConsiderationTree {
	private ArrayList<Consideration> considerations = new ArrayList<Consideration>();
	
	public void addConsideration(Consideration c) {
		considerations.add(c);
	}
	
	public Action calculateAction(GameObject obj, Game context){
		Consideration considerationBuffer = considerations.get(0);
		float buffer = considerationBuffer.evaluate(obj, context);
		
		for(int i=1; i<considerations.size(); i++) {
			float currentEval = considerations.get(i).evaluate( obj, context );
			if(currentEval>buffer) {
				considerationBuffer = considerations.get(i);
				buffer = currentEval;
			}
		}
		
		if(buffer>0)
			return considerationBuffer.getAction();	
		else
			return new Action("");
	}
}
