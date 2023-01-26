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
package multipacks.modifier.builtin.atlases.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class PermutationsSource extends AtlasSource {
	public static final String SOURCE_NAME = "paletted_permutations";

	public static final String FIELD_PALETTE_KEY = "paletteKey";
	public static final String FIELD_TEXTURES = "textures";
	public static final String FIELD_PERMUTATIONS = "permutations";

	public final ResourcePath paletteKey;
	public final List<ResourcePath> textures = new ArrayList<>();
	public final Map<String, ResourcePath> permutations = new HashMap<>();

	public PermutationsSource(ResourcePath paletteKey) {
		super("paletted_permutations");
		this.paletteKey = paletteKey;
	}

	@Override
	public JsonObject toOutputSource() {
		JsonObject json = super.toOutputSource();
		json.addProperty("palette_key", paletteKey.toString());

		JsonArray textures = new JsonArray();
		for (ResourcePath t : this.textures) textures.add(t.toString());
		json.add("textures", textures);

		JsonObject permutations = new JsonObject();
		for (Map.Entry<String, ResourcePath> e : this.permutations.entrySet()) permutations.addProperty(e.getKey(), e.getValue().toString());
		json.add("permutations", permutations);

		return json;
	}

	public static PermutationsSource sourceFromConfig(JsonObject config) {
		ResourcePath paletteKey = new ResourcePath(Selects.nonNull(config.get(FIELD_PALETTE_KEY), Messages.missingFieldAny(FIELD_PALETTE_KEY)).getAsString());
		PermutationsSource source = new PermutationsSource(paletteKey);

		for (JsonElement e : Selects.nonNull(config.get(FIELD_TEXTURES), Messages.missingFieldAny(FIELD_TEXTURES)).getAsJsonArray()) {
			source.textures.add(new ResourcePath(e.getAsString()));
		}

		for (Map.Entry<String, JsonElement> e : Selects.nonNull(config.get(FIELD_PERMUTATIONS), Messages.missingFieldAny(FIELD_PERMUTATIONS)).getAsJsonObject().entrySet()) {
			source.permutations.put(e.getKey(), new ResourcePath(e.getValue().getAsString()));
		}

		return source;
	}
}
