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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;

import multipacks.utils.ResourcePath;

public class GlyphsResult {
	public final List<Glyph> glyphs = new ArrayList<>();

	public HashMap<ResourcePath, Glyph> toIdKeyMap() {
		HashMap<ResourcePath, Glyph> map = new HashMap<>();
		for (Glyph g : glyphs) if (g.id != null) map.put(g.id, g);
		return map;
	}

	public JsonArray toJson() {
		JsonArray arr = new JsonArray();
		for (Glyph g : glyphs) arr.add(g.toJson());
		return arr;
	}
}
