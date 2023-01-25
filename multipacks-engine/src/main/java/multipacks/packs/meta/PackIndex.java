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
package multipacks.packs.meta;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.repository.query.PackQuery;
import multipacks.utils.Selects;
import multipacks.versioning.GameVersions;
import multipacks.versioning.Version;

/**
 * Pack index is where you configure your Multipacks pack. Add pack index by creating {@code multipacks.index.json}
 * in your pack root.
 * @author nahkd
 *
 */
public class PackIndex extends PackInfo {
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_DEPENDENCIES = "dependencies";
	public static final String FIELD_FEATURES = "features";

	public String description;
	public final List<PackQuery> dependencies = new ArrayList<>();
	public final List<String> features = new ArrayList<>();

	public PackIndex(String name, Version packVersion, String author, Version sourceGameVersion, String description) {
		super(name, packVersion, author, sourceGameVersion);
		this.description = description;
	}

	public PackIndex(JsonObject json) {
		super(json);

		description = Selects.getChain(json.get(FIELD_DESCRIPTION), j -> j.getAsString(), null);
		if (json.has(FIELD_DEPENDENCIES)) json.get(FIELD_DEPENDENCIES).getAsJsonArray().forEach(e -> dependencies.add(PackQuery.parse(e.getAsString())));
		if (json.has(FIELD_FEATURES)) json.get(FIELD_FEATURES).getAsJsonArray().forEach(e -> features.add(e.getAsString()));
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = super.toJson();

		if (description != null && description.trim().length() > 0) json.addProperty(FIELD_DESCRIPTION, description);
		if (dependencies.size() > 0) json.add(FIELD_DEPENDENCIES, queriesToJson());
		if (features.size() > 0) {
			JsonArray arr = new JsonArray();
			for (String feature : features) arr.add(feature);
			json.add(FIELD_FEATURES, arr);
		}

		return json;
	}

	private JsonArray queriesToJson() {
		JsonArray json = new JsonArray();
		for (PackQuery query : dependencies) json.add(query.toString());
		return json;
	}

	public JsonObject buildPackMcmeta(Version targetGameVersion) {
		JsonObject root = new JsonObject();

		JsonObject pack = new JsonObject();
		pack.addProperty("pack_format", GameVersions.getPackFormat(targetGameVersion));
		pack.addProperty("description", description != null? description : "Generated using PhoMC Multipacks");
		root.add("pack", pack);

		if (features.size() > 0) {
			JsonObject features = new JsonObject();
			JsonArray enabled = new JsonArray();
			for (String feature : this.features) enabled.add(feature);
			features.add("enabled", enabled);
			root.add("features", features);
		}

		return root;
	}
}
