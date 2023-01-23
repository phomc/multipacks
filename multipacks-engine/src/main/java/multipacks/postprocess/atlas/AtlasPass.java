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
package multipacks.postprocess.atlas;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.bundling.legacy.BundleResult;
import multipacks.logging.legacy.AbstractLogger;
import multipacks.postprocess.PostProcessPass;
import multipacks.utils.Selects;
import multipacks.vfs.Path;
import multipacks.vfs.legacy.VirtualFs;

public class AtlasPass extends PostProcessPass {
	private Path from, to;
	private AtlasTemplate[] templates;

	public AtlasPass(JsonObject config) {
		from = new Path(Selects.nonNull(config.get("from"), "'from' is empty").getAsString());
		to = new Path(Selects.nonNull(config.get("to"), "'to' is empty").getAsString());

		JsonObject templatesMap = Selects.nonNull(config.get("templates"), "'templates' is empty").getAsJsonObject();
		templates = new AtlasTemplate[templatesMap.size()];
		int pointer = 0;

		for (Entry<String, JsonElement> e : templatesMap.entrySet()) {
			templates[pointer++] = new AtlasTemplate(e.getKey(), e.getValue().getAsJsonObject());
		}
	}

	public AtlasPass(Path from, Path to, AtlasTemplate... templates) {
		this.from = from;
		this.to = to;
		this.templates = templates;
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractLogger logger) throws IOException {
		for (Path src : fs.ls(from)) {
			String[] split = src.fileName().split("\\.");
			Path base = split.length > 1? new Path(src.toString().substring(0, src.toString().length() - split[split.length - 1].length() - 1)) : src;

			InputStream in = fs.getStream(src);
			BufferedImage srcImg = ImageIO.read(in);
			in.close();

			for (AtlasTemplate t : templates) {
				Path dest = to.join(base.fileName() + t.suffix + ".png");
				BufferedImage dstImg = t.getFrom(srcImg);

				ByteArrayOutputStream o = new ByteArrayOutputStream();
				ImageIO.write(dstImg, "PNG", o);
				o.flush();
				fs.write(dest, o.toByteArray());
			}
		}

		fs.delete(from);
	}
}
