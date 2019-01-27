package engine.entity.component;

import engine.geometry.Rectangle;
import glm.vec._2.Vec2;

public class SightComponent extends Component {
	private Rectangle sightView = new Rectangle(0, 0, 0, 0);

	public SightComponent(long entityID) {
		super(entityID);
	}

	public void setViewSize(int width, int height) {
		sightView.width = width;
		sightView.height = height;
	}

	public Rectangle calculateSightView(Vec2 position) {
		sightView.x = position.x - sightView.width / 2;
		sightView.y = position.y - sightView.height / 2;
		return sightView;
	}
}
