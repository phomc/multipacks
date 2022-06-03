package multipacks.transforms.defaults.overlay;

import java.awt.image.BufferedImage;

import com.google.gson.JsonObject;

import multipacks.utils.Selects;

public class OverlaySprite {
	public final String path;
	public final int x, y;
	protected BufferedImage overlayCache;

	public OverlaySprite(String path, int x, int y) {
		this.path = path;
		this.x = x;
		this.y = y;
	}

	public OverlaySprite(JsonObject json) {
		this.path = Selects.nonNull(json.get("path"), "'path' is empty").getAsString();
		this.x = Selects.nonNull(json.get("x"), "'x' is empty").getAsInt();
		this.y = Selects.nonNull(json.get("y"), "'y' is empty").getAsInt();
	}
}
