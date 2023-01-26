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

import com.google.gson.JsonObject;

public abstract class AtlasSource {
	public final String type;

	public AtlasSource(String type) {
		this.type = type;
	}

	public JsonObject toOutputSource() {
		JsonObject json = new JsonObject();
		json.addProperty("type", type);
		return json;
	}

	public static AtlasSource sourceFromConfig(String name, JsonObject config) {
		return switch (name) {
		case DirectorySource.SOURCE_NAME -> DirectorySource.sourceFromConfig(config);
		case SingleSource.SOURCE_NAME -> SingleSource.sourceFromConfig(config);
		case FilterSource.SOURCE_NAME -> FilterSource.sourceFromConfig(config);
		case PermutationsSource.SOURCE_NAME -> PermutationsSource.sourceFromConfig(config);
		default -> null;
		};
	}
}
