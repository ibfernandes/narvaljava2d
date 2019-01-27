package engine.entity.component;

import engine.controllers.Controller;

public class ControllerComponent extends Component {
	private Controller controller;

	public ControllerComponent(long entityID) {
		super(entityID);

	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
}
