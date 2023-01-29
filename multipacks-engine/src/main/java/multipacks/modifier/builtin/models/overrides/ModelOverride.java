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
package multipacks.modifier.builtin.models.overrides;

import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.modifier.builtin.models.BaseItemModel;
import multipacks.utils.ResourcePath;

/**
 * @author nahkd
 *
 */
public abstract class ModelOverride {
	public final BaseItemModel base;
	public final ResourcePath modelPath;

	public ModelOverride(BaseItemModel base, ResourcePath modelPath) {
		this.base = base;
		this.modelPath = modelPath;
	}

	public abstract JsonObject toPredicateJson();

	public JsonObject toOverrideJson() {
		JsonObject json = new JsonObject();
		json.add("predicate", toPredicateJson());
		json.addProperty("model", modelPath.toString());
		return json;
	}

	public static ModelOverride fromOverrideJson(BaseItemModel base, JsonObject json) {
		JsonObject predicate = json.get("predicate").getAsJsonObject();
		ResourcePath model = new ResourcePath(json.get("model").getAsString());

		Optional<Map.Entry<String, JsonElement>> optional = predicate.entrySet().stream().findFirst();
		if (optional.isPresent()) {
			Map.Entry<String, JsonElement> entry = optional.get();
			String predicateType = entry.getKey();
			JsonElement predicateValue = entry.getValue();

			return switch (predicateType) {
			case CustomModelOverride.PREDICATE_TYPE -> new CustomModelOverride(base, model, predicateValue.getAsInt());
			default -> new UnknownModelOverride(base, model, predicateType, predicateValue.getAsDouble());
			};
		} else {
			return null;
		}
	}
}
