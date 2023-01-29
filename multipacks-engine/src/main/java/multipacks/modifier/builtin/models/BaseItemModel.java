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
package multipacks.modifier.builtin.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.modifier.builtin.models.overrides.CustomModelOverride;
import multipacks.modifier.builtin.models.overrides.ModelOverride;
import multipacks.modifier.builtin.models.overrides.TrimModelOverride;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class BaseItemModel {
	public final ResourcePath itemId;
	public final JsonObject modelContent;
	public final List<ModelOverride> overrides = new ArrayList<>();
	public final Map<ResourcePath, ModelOverride> namedOverrides = new HashMap<>();

	private int lastModelId = 1;
	private HashSet<Double> occupiedTrims = new HashSet<>();

	public BaseItemModel(ResourcePath itemId, JsonObject modelContent) {
		this.itemId = itemId;
		this.modelContent = modelContent;
		addExistingOverrides();
	}

	private void addExistingOverrides() {
		JsonArray arr = Selects.getChain(modelContent.get("overrides"), j -> j.getAsJsonArray(), new JsonArray());

		for (JsonElement e : arr) {
			ModelOverride override = ModelOverride.fromOverrideJson(this, e.getAsJsonObject());
			overrides.add(override);

			if (override instanceof CustomModelOverride cmo) lastModelId = Math.max(lastModelId, cmo.modelId + 1);
			if (override instanceof TrimModelOverride tmo) occupiedTrims.add(tmo.trimType);
		}
	}

	public CustomModelOverride allocateCustomModelId(ResourcePath model, ResourcePath namedOverride) {
		int id = lastModelId++;
		CustomModelOverride out = new CustomModelOverride(this, model, id);

		overrides.add(out);
		if (namedOverride != null) namedOverrides.put(namedOverride, out);
		return out;
	}

	public TrimModelOverride allocateTrim(ResourcePath model, ResourcePath namedOverride) {
		double val;
		do { val = Math.random(); } while (occupiedTrims.contains(val));
		TrimModelOverride out = new TrimModelOverride(this, model, val);

		overrides.add(out);
		if (namedOverride != null) namedOverrides.put(namedOverride, out);
		return out;
	}

	public JsonObject toModelJson() {
		JsonArray overrides = new JsonArray();
		for (ModelOverride override : this.overrides) overrides.add(override.toOverrideJson());
		modelContent.add("overrides", overrides);
		return modelContent;
	}
}
