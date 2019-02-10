package engine.entity.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import engine.controllers.Controller;

public class ControllerComponent extends Component {
	private transient Controller controller;

	public ControllerComponent(long entityID) {
		super(entityID);
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	private void writeObject(ObjectOutputStream os) throws IOException {
		os.defaultWriteObject();
		os.writeObject(controller.getClass().getName());
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		ois.defaultReadObject();
		String className = (String) ois.readObject();
		controller = (Controller) Class.forName(className).newInstance();

	}
}
