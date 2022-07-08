package multipacks.postprocess.atlas;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.google.gson.JsonObject;

import multipacks.utils.Selects;

public class AtlasTemplate {
	public final String suffix;
	public final int x, y, width, height;

	public AtlasTemplate(String suffix, int x, int y, int width, int height) {
		this.suffix = suffix;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public AtlasTemplate(String suffix, JsonObject json) {
		this.suffix = suffix;
		this.x = Selects.nonNull(json.get("x"), "'x' is empty").getAsInt();
		this.y = Selects.nonNull(json.get("y"), "'y' is empty").getAsInt();
		this.width = Selects.nonNull(json.get("width"), "'width' is empty").getAsInt();
		this.height = Selects.nonNull(json.get("height"), "'height' is empty").getAsInt();
	}

	public BufferedImage getFrom(BufferedImage img) {
		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = dest.createGraphics();
		g.drawImage(img, 0, 0, width, height, x, y, x + width, y + height, null);
		g.dispose();
		return dest;
	}
}
