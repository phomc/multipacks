package multipacks.postprocess.overlays;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleResult;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;
import multipacks.vfs.Path;
import multipacks.vfs.VirtualFs;

public class OverlayPass extends PostProcessPass {
	private Path target;
	private Overlay[] overlays;

	public OverlayPass(JsonObject config) {
		target = new Path(Selects.nonNull(config.get("target"), "'target' is empty").getAsString());
		JsonArray arr = Selects.nonNull(config.get("overlays"), "'overlays' is empty").getAsJsonArray();
		overlays = new Overlay[arr.size()];
		for (int i = 0; i < overlays.length; i++) overlays[i] = new Overlay(arr.get(i));
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (Overlay overlay : overlays) {
			InputStream in = fs.getStream(overlay.overlayFile);
			if (in == null) {
				logger.warning("Overlay " + overlay.overlayFile + " doesn't exists, skipping...");
				continue;
			}

			overlay.cachedImage = ImageIO.read(in);
			in.close();
		}

		for (Path targetFile : fs.ls(target)) {
			InputStream in = fs.getStream(targetFile);
			BufferedImage imgTarget = ImageIO.read(in);
			in.close();

			BufferedImage imgOut = new BufferedImage(imgTarget.getWidth(), imgTarget.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = imgOut.createGraphics();
			g.drawImage(imgTarget, null, 0, 0);

			for (Overlay overlay : overlays) if (overlay.cachedImage != null) g.drawImage(overlay.cachedImage, null, overlay.x, overlay.y);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(imgOut, "PNG", out);
			fs.write(targetFile, out.toByteArray());
			System.out.println(targetFile);
		}
	}
}
