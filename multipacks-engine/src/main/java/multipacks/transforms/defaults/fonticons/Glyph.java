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

import com.google.gson.JsonObject;

import multipacks.utils.ResourcePath;

public class Glyph {
	public final ResourcePath id;
	public final char character;
	public final ResourcePath font;
	public final ResourcePath texture;

	public Glyph(ResourcePath id, char character, ResourcePath font, ResourcePath texture) {
		this.id = id;
		this.character = character;
		this.font = font;
		this.texture = texture;
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (id != null) json.addProperty("id", id.toString());
		json.addProperty("charCode", (int) character);
		json.addProperty("font", font.toString());
		json.addProperty("texture", texture.toString());
		return json;
	}
}
