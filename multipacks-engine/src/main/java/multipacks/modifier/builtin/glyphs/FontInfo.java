/*
 * Copyright (c) 2022-2023 PhoMC
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

import java.util.HashMap;

import com.google.gson.JsonArray;

import multipacks.utils.ResourcePath;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class FontInfo {
	public static final String ERROR_OUT_OF_SPACES = "Out of spaces for next character";

	public final ResourcePath id;
	public final HashMap<ResourcePath, Glyph> glyphs = new HashMap<>();
	public final HashMap<Character, Glyph> assignedGlyphs = new HashMap<>();

	// File output
	protected JsonArray providers = new JsonArray();
	protected HashMap<Character, Integer> spaceWidths = new HashMap<>();

	public FontInfo(ResourcePath id) {
		this.id = id;
	}

	public Vfs touchFontDeclaration(Vfs root) {
		return root.mkdir("assets").mkdir(id.namespace).mkdir("font").touch(id.path + ".json");
	}
}
