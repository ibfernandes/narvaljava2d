package engine.entity.component;

import engine.controllers.Controller;

public class ControllerComponent extends Component{
	public ControllerComponent(long entityID) {
		super(entityID);
		
	}

	public Controller controller;
}
