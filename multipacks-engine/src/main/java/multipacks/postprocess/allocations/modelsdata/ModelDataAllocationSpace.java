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
package multipacks.postprocess.allocations.modelsdata;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.postprocess.allocations.AllocationSpace;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.vfs.Path;
import multipacks.vfs.VirtualFs;

public class ModelDataAllocationSpace extends AllocationSpace<AllocatedModelData, Path> {
	public ResourcePath itemId;
	public int startId, endId;

	public ModelDataAllocationSpace(ResourcePath itemId, int startId, int endId) {
		this.itemId = itemId;
		this.startId = startId;
		this.endId = endId;
	}

	public ModelDataAllocationSpace(JsonObject json) {
		itemId = new ResourcePath(Selects.nonNull(json.get("id"), "'id' is empty").getAsString());
		startId = Selects.getChain(json.get("start"), j -> j.getAsInt(), 1);
		endId = Selects.getChain(json.get("end"), j -> j.getAsInt(), startId);
	}

	@Override
	public int size() { return endId - startId + 1; }

	@Override
	public AllocatedModelData allocate(int index, VirtualFs fs, Path model) throws IOException {
		int modelId = startId + index;

		Path modelPath = new Path("assets", itemId.namespace, "models", "item", itemId.path + ".json");
		JsonObject json = fs.isExists(modelPath)? fs.readJson(modelPath).getAsJsonObject() : new JsonObject();

		if (!fs.isExists(modelPath)) {
			// TODO: allow modifying these values from json
			json.addProperty("parent", "item/handheld");

			JsonObject textures = new JsonObject();
			textures.addProperty("layer0", itemId.namespace + ":" + "item/" + itemId.path);
			json.add("textures", textures);
		}

		JsonArray overrides = json.has("overrides")? json.get("overrides").getAsJsonArray() : new JsonArray();
		JsonObject override = new JsonObject();
		JsonObject predicate = new JsonObject();
		predicate.addProperty("custom_model_data", modelId);
		override.add("predicate", predicate);
		override.addProperty("model", model.toNamespacedKey(3).toString());
		overrides.add(override);
		json.add("overrides", overrides);
		fs.writeJson(modelPath, json);

		return new AllocatedModelData(itemId, modelId);
	}
}
