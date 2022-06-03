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
package multipacks.transforms.defaults.fonticons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import multipacks.bundling.BundleResult;
import multipacks.transforms.TransformPass;
import multipacks.transforms.TransformativeFileSystem;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.utils.StringUtils;
import multipacks.utils.logging.AbstractMPLogger;

public class FontIconsTransformPass extends TransformPass {
	public String[] sources;
	public ResourcePath targetFont;
	public ResourcePath targetTextures;
	public char[][] targetMappings;
	public String resultMappingPath;

	public FontIconsTransformPass(JsonObject json) {
		JsonArray arr = Selects.nonNull(json.get("source"), "'source' is empty").getAsJsonArray();
		sources = new String[arr.size()];
		for (int i = 0; i < sources.length; i++) sources[i] = arr.get(i).getAsString();

		JsonObject target = Selects.nonNull(json.get("target"), "'target' is empty").getAsJsonObject();
		targetFont = new ResourcePath(Selects.nonNull(target.get("font"), "'target.font' is empty").getAsString());
		targetTextures = new ResourcePath(Selects.nonNull(target.get("textures"), "'target.textures' is empty").getAsString());

		if (target.has("mapping")) {
			JsonArray targetMappingArr = target.get("mapping").getAsJsonArray();
			targetMappings = new char[targetMappingArr.size()][];

			for (int i = 0; i < targetMappings.length; i++) {
				JsonObject mapping = targetMappingArr.get(i).getAsJsonObject();
				char fromChar = Selects.getChain(mapping.get("from"), t -> t.getAsString().charAt(0), '\u0001');
				char toChar = Selects.getChain(mapping.get("to"), t -> t.getAsString().charAt(0), '\uffff');
				targetMappings[i] = new char[] { fromChar, toChar };
			}
		}

		resultMappingPath = Selects.getChain(json.get("charsMapping"), v -> v.getAsString(), null);
	}

	private int getMappedCharAtIndex(int i) {
		for (int j = 0; j < targetMappings.length; j++) {
			char from = targetMappings[j][0];
			char to = targetMappings[j][1];
			int range = to - from;

			if (i < range) return from + i;
			i -= range;
		}

		return -1;
	}

	@Override
	public void transform(TransformativeFileSystem fs, BundleResult result, AbstractMPLogger logger) throws IOException {
		List<JsonObject> rawGlyphs = new ArrayList<>();
		GlyphsResult glyphsResult = result.getOrCreate(GlyphsResult.class, GlyphsResult::new);

		for (String source : sources) {
			for (String p : fs.ls(source)) {
				if (p.endsWith(".json")) {
					InputStream in = fs.openRead(source + "/" + p);
					JsonParser parser = new JsonParser();
					JsonObject obj = parser.parse(new InputStreamReader(in)).getAsJsonObject();
					in.close();
					rawGlyphs.add(obj);
					fs.delete(source + "/" + p);

					if (!obj.has("id")) logger.warning(source + "/" + p + ": 'id' field not found, may generates random file name");
				}
			}
		}

		JsonObject outputJson = new JsonObject();
		JsonArray prov = new JsonArray();
		int charIndex = 0;

		for (JsonObject rawGlyph : rawGlyphs) {
			int ch = getMappedCharAtIndex(charIndex++);
			ResourcePath id = Selects.getChain(rawGlyph.get("id"), v -> new ResourcePath(v.getAsString()), null);
			ResourcePath texture = new ResourcePath(Selects.nonNull(rawGlyph.get("texture"), "'texture' is empty").getAsString());
			int ascent = Selects.nonNull(rawGlyph.get("ascent"), "'ascent' is empty").getAsInt();
			int height = Selects.nonNull(rawGlyph.get("height"), "'height' is empty").getAsInt();

			String generatedFileName = id != null? id.namespace + "_" + id.path : StringUtils.randomString(24);
			ResourcePath mappedTexture = new ResourcePath(targetTextures + "/" + generatedFileName + ".png"); // TODO: Generate readable part
			String sourceTexturePath = "assets/" + texture.namespace + "/" + texture.path + ".png";
			String mappedTexturePath = "assets/" + mappedTexture.namespace + "/textures/" + mappedTexture.path;

			byte[] source = fs.get(sourceTexturePath);

			if (source == null) {
				logger.warning(sourceTexturePath + " doesn't exists, font icon for this texture will be skipped");
				continue;
			}

			fs.put(mappedTexturePath, source);
			fs.delete(sourceTexturePath);

			JsonObject glyph = new JsonObject();
			glyph.addProperty("type", "bitmap");
			glyph.addProperty("file", mappedTexture.toString());
			glyph.addProperty("ascent", ascent);
			glyph.addProperty("height", height);

			JsonArray chars = new JsonArray();
			chars.add((char) ch);
			glyph.add("chars", chars);
			prov.add(glyph);

			glyphsResult.glyphs.add(new Glyph(id != null? id : null, (char) ch, targetFont, mappedTexture));
		}

		outputJson.add("providers", prov);
		String fontJsonPath = "assets/" + targetFont.namespace + "/font/" + targetFont.path + ".json";

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(out);
		new Gson().toJson(outputJson, new JsonWriter(writer));
		writer.flush();
		fs.put(fontJsonPath, out.toByteArray());

		if (resultMappingPath != null) {
			out = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(out);
			new Gson().toJson(glyphsResult.toJson(), new JsonWriter(writer));
			writer.flush();
			fs.put(resultMappingPath, out.toByteArray());
		}
	}
}
