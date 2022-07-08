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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import multipacks.utils.Selects;

public class GlyphMapping {
	public final char from;
	public final char to;

	public GlyphMapping(char from, char to) {
		this.from = from;
		this.to = to;
	}

	public GlyphMapping(JsonElement json) {
		if (json.isJsonPrimitive()) from = to = json.getAsCharacter();
		else if (json.isJsonObject()) {
			from = Selects.nonNull(json.getAsJsonObject().get("from"), "'from' is empty").getAsCharacter();
			to = Selects.nonNull(json.getAsJsonObject().get("to"), "'to' is empty").getAsCharacter();
		} else if (json.isJsonArray()) {
			JsonArray arr = json.getAsJsonArray();
			char a = arr.get(0).getAsCharacter();
			char b = arr.get(1).getAsCharacter();
			from = a > b? b : a;
			to = a > b? a : b;
		} else throw new JsonSyntaxException("Cannot convert " + json + " to characters mapping for glyphs");
	}

	public int size() { return to - from + 1; }
	public int get(int ptr) { return ptr < size()? from + ptr : -1; }
}
