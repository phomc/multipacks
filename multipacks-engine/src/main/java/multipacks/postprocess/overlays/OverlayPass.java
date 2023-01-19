/*
 * Copyright (c) 2020-2022 MangoPlex
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package multipacks.postprocess.overlays;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.bundling.legacy.BundleResult;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;
import multipacks.vfs.Path;
import multipacks.vfs.legacy.VirtualFs;

public class OverlayPass extends PostProcessPass {
	private Path target;
	private Overlay[] overlays;

	public OverlayPass(JsonObject config) {
		target = new Path(Selects.nonNull(config.get("target"), "'target' is empty").getAsString());
		JsonArray arr = Selects.nonNull(config.get("overlays"), "'overlays' is empty").getAsJsonArray();
		overlays = new Overlay[arr.size()];
		for (int i = 0; i < overlays.length; i++) overlays[i] = new Overlay(arr.get(i));
	}

	public OverlayPass(Path target, Overlay... overlays) {
		this.target = target;
		this.overlays = overlays;
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		for (Overlay overlay : overlays) {
			if (overlay.overlayFile == null) continue;

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
