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
package multipacks.modifier.builtin.glyphs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import multipacks.modifier.Modifier;
import multipacks.modifier.ModifiersAccess;
import multipacks.packs.Pack;
import multipacks.utils.Constants;
import multipacks.utils.Holder;
import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.utils.io.IOUtils;
import multipacks.vfs.Path;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class GlyphsModifier extends Modifier {
	public static final ResourcePath ID = new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/glyphs");

	public static final String ERROR_OUT_OF_SPACES = "Out of spaces for next character";

	public static final String FIELD_INCLUDE = "include";
	public static final String FIELD_ALLOCATE = "allocate";
	public static final String FIELD_ID = "id";

	public static final String FIELD_BITMAP = "bitmap";
	public static final String FIELD_ASCENT = "ascent";
	public static final String FIELD_HEIGHT = "height";

	public static final String FIELD_SPACE = "space";

	public final HashMap<ResourcePath, FontInfo> fonts = new HashMap<>();
	public final HashMap<ResourcePath, Glyph> glyphs = new HashMap<>();

	@Override
	public void applyModifier(Pack fromPack, Vfs contents, JsonElement config, ModifiersAccess access) {
		if (config == null) return; // TODO: warn missing configuration

		// Providers: bitmap, space
		addGlyphsFor(contents, contents, null, new ArrayList<>(), config);
	}

	@Override
	public void finalizeModifier(Vfs contents, ModifiersAccess access) {
		// TODO: Write here
		// assets/namespace/font/id.json
		for (FontInfo font : fonts.values()) {
			Vfs fontFile = font.touchFontDeclaration(contents);

			JsonObject spaceProvider = new JsonObject();
			JsonObject spaceAdvances = new JsonObject();

			for (Map.Entry<Character, Integer> entry : font.spaceWidths.entrySet()) {
				char ch = entry.getKey();
				int width = entry.getValue();
				spaceAdvances.addProperty(String.valueOf(ch), width);
			}

			spaceProvider.addProperty("type", "space");
			spaceProvider.add("advances", spaceAdvances);
			font.providers.add(spaceProvider);

			JsonObject root = new JsonObject();
			root.add("providers", font.providers);

			try {
				IOUtils.jsonToVfs(root, fontFile);
			} catch (IOException e) {
				throw new RuntimeException("Failed to write JSON data to " + fontFile, e);
			}

			font.providers = null;
			font.spaceWidths = null;
		}
	}

	@Override
	public void serializeModifier(DataOutput output, ModifiersAccess access) throws IOException {
		for (Glyph g : glyphs.values()) {
			output.writeUTF(g.glyphId.toString());
			output.writeUTF(g.font.id.toString());
			output.writeChar(g.assigned);
		}

		output.writeUTF(""); // 0-length string: End of List
	}

	public static GlyphsModifier deserializeModifier(DataInput input) throws IOException {
		String glyphIdStr;
		GlyphsModifier modifier = new GlyphsModifier();

		while ((glyphIdStr = input.readUTF()).length() > 0) {
			ResourcePath glyphId = new ResourcePath(glyphIdStr);
			ResourcePath fontId = new ResourcePath(input.readUTF());
			char assigned = input.readChar();

			FontInfo font = modifier.fonts.get(fontId);

			if (font == null) {
				modifier.fonts.put(fontId, font = new FontInfo(fontId));
				font.providers = null;
				font.spaceWidths = null;
			}

			Glyph g = new Glyph(glyphId, font, assigned);

			font.glyphs.put(fontId, g);
			font.assignedGlyphs.put(assigned, g);
			modifier.glyphs.put(glyphId, g);
		}

		return modifier;
	}

	public static void registerTo(ModifiersAccess access) {
		access.registerModifier(ID, GlyphsModifier::new, GlyphsModifier::deserializeModifier);
	}

	private void addGlyphsFor(Vfs root, Vfs scoped, Vfs configFile, List<GlyphsAllocation> allocations, JsonElement config) {
		if (config.isJsonArray()) {
			JsonArray arr = config.getAsJsonArray();
			for (JsonElement e : arr) addGlyphsFor(root, scoped, configFile, allocations, e);
		} else if (config.isJsonObject()) {
			JsonObject obj = config.getAsJsonObject();
			if (obj.has(FIELD_INCLUDE)) {
				Path includePath = new Path(Selects.nonNull(obj.get(FIELD_INCLUDE), Messages.missingFieldAny(FIELD_INCLUDE)).getAsString());
				Vfs file = scoped.get(includePath);

				try {
					JsonElement includeConfig = IOUtils.jsonFromVfs(file);
					addGlyphsFor(root, file.get(".."), file, allocations, includeConfig);
				} catch (IOException e) {
					throw new RuntimeException("Failed to obtain JSON data for " + file, e);
				}
			} else if (obj.has(FIELD_ALLOCATE)) {
				JsonArray arr = obj.get(FIELD_ALLOCATE).getAsJsonArray();

				for (JsonElement e : arr) {
					GlyphsAllocation allocation = new GlyphsAllocation(fonts, e.getAsJsonObject());
					allocations.add(allocation);
				}
			} else if (obj.has(FIELD_ID)) {
				ResourcePath id = new ResourcePath(obj.get(FIELD_ID).getAsString());

				if (obj.has(FIELD_BITMAP)) {
					Vfs bitmapFile = scoped.get(new Path(Selects.nonNull(obj.get(FIELD_BITMAP), Messages.missingFieldAny(FIELD_BITMAP)).getAsString()));
					int ascent = Selects.nonNull(obj.get(FIELD_ASCENT), Messages.missingFieldAny(FIELD_ASCENT)).getAsInt();
					int height = Selects.getChain(obj.get(FIELD_HEIGHT), j -> j.getAsInt(), 8);

					// TODO: remap bitmap file to somewhere...
					// assets/<namespace>/textures/font/multipacks_<ID>.png
					// <namespace>:font/multipacks_<ID>.png
					Vfs bitmapDest = root.mkdir("assets").mkdir(id.namespace).mkdir("textures").mkdir("font").touch("multipacks_" + id.path + ".png");
					try (InputStream in = bitmapFile.getInputStream()) {
						try (OutputStream out = bitmapDest.getOutputStream()) {
							in.transferTo(out);
						}
					} catch (IOException e) {
						throw new RuntimeException("Failed to copy from " + bitmapFile + " to " + bitmapDest, e);
					}

					bitmapFile.getParent().delete(bitmapFile.getName());

					Holder<FontInfo> fontPtr = new Holder<>(null);
					char ch = findNextSuitableChar(allocations, fontPtr);
					Glyph g = new Glyph(id, fontPtr.value, ch);
					glyphs.put(id, g);

					JsonObject bitmapProvider = new JsonObject();
					bitmapProvider.addProperty("type", "bitmap");
					bitmapProvider.addProperty("file", id.namespace + ":font/multipacks_" + id.path + ".png");
					bitmapProvider.addProperty("ascent", ascent);
					bitmapProvider.addProperty("height", height);

					JsonArray bitmapChars = new JsonArray();
					bitmapChars.add(ch);
					bitmapProvider.add("chars", bitmapChars);

					fontPtr.value.providers.add(bitmapProvider);
				} else if (obj.has(FIELD_SPACE)) {
					int width = obj.get(FIELD_SPACE).getAsInt();

					Holder<FontInfo> fontPtr = new Holder<>(null);
					char ch = findNextSuitableChar(allocations, fontPtr);
					Glyph g = new Glyph(id, fontPtr.value, ch);
					glyphs.put(id, g);

					fontPtr.value.spaceWidths.put(ch, width);
				} else throw new JsonSyntaxException(Messages.missingFieldAny(FIELD_BITMAP, FIELD_SPACE));

			} else throw new JsonSyntaxException(Messages.missingFieldAny(FIELD_INCLUDE, FIELD_ALLOCATE, FIELD_ID));
		}

		if (configFile != null) configFile.getParent().delete(configFile.getName());
	}

	private char findNextSuitableChar(List<GlyphsAllocation> allocations, Holder<FontInfo> fontPtr) {
		if (allocations.size() == 0) throw new IllegalArgumentException(ERROR_OUT_OF_SPACES);
		int allocIdx = 0;
		GlyphsAllocation currentAlloc = allocations.get(0);

		Character ch;
		while ((ch = currentAlloc.findSuitableNextChar()) == null) {
			allocIdx++;
			if (allocIdx >= allocations.size()) throw new IllegalArgumentException(ERROR_OUT_OF_SPACES);
			currentAlloc = allocations.get(allocIdx);
		}

		fontPtr.value = currentAlloc.font;
		return ch;
	}
}
