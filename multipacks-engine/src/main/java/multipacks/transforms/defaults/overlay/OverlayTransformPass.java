package multipacks.transforms.defaults.overlay;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

public class OverlayTransformPass extends TransformPass {
	private String target;
	private OverlaySprite[] overlays;

	public OverlayTransformPass(JsonObject json) {
		target = Selects.nonNull(json.get("target"), "'target' is empty").getAsString();
		JsonArray overlaysArr = Selects.nonNull(json.get("overlays"), "'overlays' is empty").getAsJsonArray();
		overlays = new OverlaySprite[overlaysArr.size()];
		for (int i = 0; i < overlays.length; i++) overlays[i] = new OverlaySprite(overlaysArr.get(i).getAsJsonObject());
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (OverlaySprite o : overlays) {
			InputStream stream = fs.openRead(o.path);

			if (stream == null) {
				logger.warning("File " + o.path + " doesn't exists, skipping this overlay...");
				continue;
			}

			o.overlayCache = ImageIO.read(stream);
			stream.close();
		}

		for (String path : fs.lsFullPath(target)) {
			InputStream stream = fs.openRead(path);
			BufferedImage imgTarget = ImageIO.read(stream);
			BufferedImage imgOut = new BufferedImage(imgTarget.getWidth(), imgTarget.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			stream.close();

			Graphics2D g = imgOut.createGraphics();
			g.drawImage(imgTarget, null, 0, 0);
			for (OverlaySprite o : overlays) if (o.overlayCache != null) g.drawImage(o.overlayCache, o.x, o.y, null);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(imgOut, "PNG", out);
			out.flush();
			fs.put(path, out.toByteArray());
		}
	}
}
