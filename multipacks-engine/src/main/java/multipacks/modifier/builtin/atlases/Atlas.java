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
package multipacks.modifier.builtin.atlases;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.modifier.builtin.atlases.sources.AtlasSource;
import multipacks.utils.ResourcePath;
import multipacks.vfs.Path;

/**
 * @author nahkd
 *
 */
public class Atlas {
	public final ResourcePath id;
	public final List<AtlasSource> sources = new ArrayList<>();

	public Atlas(ResourcePath id) {
		this.id = id;
	}

	public Path getTargetPath() {
		return new Path("assets/" + id.namespace + "/atlases/" + id.path + ".json");
	}

	public JsonObject toOutput() {
		JsonObject json = new JsonObject();

		JsonArray sources = new JsonArray();
		for (AtlasSource s : this.sources) sources.add(s.toOutputSource());
		json.add("sources", sources);

		return json;
	}
}
