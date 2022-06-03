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
