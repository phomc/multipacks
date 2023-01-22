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
package multipacks.postprocess.font;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.bundling.legacy.BundleResult;
import multipacks.logging.legacy.AbstractLogger;
import multipacks.postprocess.PostProcessPass;
import multipacks.postprocess.ProcessFailException;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.vfs.Path;
import multipacks.vfs.legacy.VirtualFs;

public class FontIconsPass extends PostProcessPass {
	private Path from;
	private Path to;
	private Path textures;
	private GlyphMapping[] mapping;

	private int currentChar = 0;

	public FontIconsPass(JsonObject config) {
		from = new Path(Selects.nonNull(config.get("from"), "'from' is empty").getAsString());
		to = new Path(Selects.nonNull(config.get("to"), "'to' is empty").getAsString());
		textures = new Path(Selects.nonNull(config.get("textures"), "'textures' is empty").getAsString());

		JsonArray arr = Selects.nonNull(config.get("mapping"), "'mapping' is empty").getAsJsonArray();
		mapping = new GlyphMapping[arr.size()];
		for (int i = 0; i < mapping.length; i++) mapping[i] = new GlyphMapping(arr.get(i));
	}

	public FontIconsPass(Path from, Path to, Path texturesOutput, GlyphMapping... mapping) {
		this.from = from;
		this.to = to;
		this.textures = texturesOutput;
		this.mapping = mapping;
	}

	private int nextChar() {
		for (GlyphMapping m : mapping) {
			if (currentChar >= m.size()) {
				currentChar -= m.size();
				continue;
			}

			return m.get(currentChar++);
		}
		return -1;
	}

	@Override
	public void apply(VirtualFs fs, BundleResult result, AbstractLogger logger) throws IOException {
		JsonArray providers = new JsonArray();
		GlyphsMap glyphs = result.getOrCreate(GlyphsMap.class, GlyphsMap::new);
		ResourcePath fontId = to.toNamespacedKey(3).noFileExtension();

		for (Path p : fs.ls(from)) {
			if (!p.toString().endsWith(".json")) continue;
			JsonObject json = fs.readJson(p).getAsJsonObject();

			ResourcePath glyphId = new ResourcePath(Selects.getChain(json.get("id"), j -> j.getAsString(), "multipacks-unnamed:" + hashOf(p.toString())));
			Path texture = p.parent().join(Selects.nonNull(json.get("texture"), "'texture' is missing in " + p).getAsString());
			int ascent = Selects.nonNull(json.get("ascent"), "'ascent' is missing in " + p).getAsInt();
			int height = Selects.nonNull(json.get("height"), "'height' is missing in " + p).getAsInt();

			Path textureOutput = textures.join(glyphId.toString().replaceAll(":", "_") + ".png");
			int charInt = nextChar();
			if (charInt == -1) throw new ProcessFailException("Not enough mapped characters to process font icons glyphs");
			char ch = (char) charInt;

			JsonObject prov = new JsonObject();
			prov.addProperty("type", "bitmap");
			prov.addProperty("file", textureOutput.toNamespacedKey(3).toString());
			prov.addProperty("ascent", ascent);
			prov.addProperty("height", height);

			JsonArray chars = new JsonArray();
			chars.add(ch);
			prov.add("chars", chars);

			providers.add(prov);
			fs.write(textureOutput, fs.read(texture));
			glyphs.put(glyphId, new Glyph(fontId, glyphId, ch, ascent, height));
		}

		JsonObject obj = new JsonObject();
		obj.add("providers", providers);
		fs.writeJson(to, obj);
		fs.delete(from);
	}

	private static char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private static String hashOf(String text) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(text.getBytes(StandardCharsets.UTF_8));
			byte[] bs = digest.digest();
			char[] cs = new char[bs.length * 2];
			for (int i = 0; i < bs.length; i++) {
				cs[i * 2 + 0] = HEX[(Byte.toUnsignedInt(bs[i]) & 0xF0) >> 4];
				cs[i * 2 + 1] = HEX[(Byte.toUnsignedInt(bs[i]) & 0x0F) >> 0];
			}
			return String.valueOf(cs);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return (Math.random() + "").replace('.', '-');
		}
	}
}
