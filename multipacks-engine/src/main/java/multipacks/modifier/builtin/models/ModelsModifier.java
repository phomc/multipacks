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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import multipacks.modifier.ModifiersAccess;
import multipacks.modifier.builtin.BuiltinModifierBase;
import multipacks.packs.Pack;
import multipacks.utils.Constants;
import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.utils.io.IOUtils;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class ModelsModifier extends BuiltinModifierBase<Void> {
	public static final String ERROR_MISSING_MODEL_A = "Missing model JSON for ";

	public static final ResourcePath ID = new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/models");

	public static final String FIELD_INCLUDE = "include";
	public static final String FIELD_ID = "id";
	public static final String FIELD_MODEL = "model";
	public static final String FIELD_ITEM = "item";

	public static final ResourcePath DEFAULT_ITEM_ID = new ResourcePath(Constants.GAME_NAMESPACE, "barrier");

	public final Map<ResourcePath, ItemModels> items = new HashMap<>();
	public final Map<ResourcePath, Model> models = new HashMap<>();

	@Override
	public void finalizeModifier(Vfs contents, ModifiersAccess access) {
	}

	@Override
	public void serializeModifier(DataOutput output, ModifiersAccess access) throws IOException {
		for (Model model : models.values()) {
			output.writeUTF(model.item.itemId.toString());
			output.writeInt(model.modelId);
		}

		output.writeUTF("");
	}

	public static ModelsModifier deserializeModifier(DataInput input) throws IOException {
		String itemIdStr;
		ModelsModifier mod = new ModelsModifier();

		while ((itemIdStr = input.readUTF()).length() > 0) {
			ResourcePath itemId = new ResourcePath(itemIdStr);
			int modelId = input.readInt();

			ItemModels item = mod.items.get(itemId);
			if (item == null) mod.items.put(itemId, item = new ItemModels(itemId));

			Model model = new Model(item, modelId);
			item.models.put(itemId, model);
			mod.models.put(itemId, model);
		}

		return mod;
	}

	public static void registerTo(ModifiersAccess access) {
		access.registerModifier(ID, ModelsModifier::new, ModelsModifier::deserializeModifier);
	}

	@Override
	protected void applyWithScopedConfig(Pack fromPack, Vfs root, Vfs scoped, JsonElement config, Void data, ModifiersAccess access) {
		if (config.isJsonObject()) {
			JsonObject obj = config.getAsJsonObject();

			if (obj.has(FIELD_ID)) {
				ResourcePath id = new ResourcePath(obj.get(FIELD_ID).getAsString());
				ResourcePath modelId = new ResourcePath(Selects.nonNull(obj.get(FIELD_MODEL), Messages.missingFieldAny(FIELD_MODEL)).getAsString());
				ResourcePath itemId = new ResourcePath(Selects.nonNull(obj.get(FIELD_ITEM), Messages.missingFieldAny(FIELD_ITEM)).getAsString());
				Vfs targetModelFile = root.mkdir("assets").mkdir(itemId.namespace).mkdir("models").mkdir("item").get(itemId.path + ".json");

				// TODO: Obtain model from user's game installation (if exists)
				// We'll obtain it from ~/.minecraft/versions/<Version>/<version>.jar:assets/...
				// Or we can obtain it by downloading the JAR included inside version JSON file
				if (targetModelFile == null) throw new RuntimeException(ERROR_MISSING_MODEL_A + itemId.namespace + ":item/" + itemId.path);

				ItemModels item = items.get(itemId);
				if (item == null) items.put(itemId, item = new ItemModels(itemId));
				int idx = item.currentIdx++;

				try {
					JsonObject targetModel = IOUtils.jsonFromVfs(targetModelFile).getAsJsonObject();

					JsonArray overrides = Selects.getChain(targetModel.get("overrides"), j -> j.getAsJsonArray(), new JsonArray());
					JsonObject override = new JsonObject();
					JsonObject predicate = new JsonObject();
					predicate.addProperty("custom_model_data", idx);
					override.add("predicate", predicate);
					override.addProperty("model", modelId.toString());
					overrides.add(override);
					targetModel.add("overrides", overrides);

					IOUtils.jsonToVfs(targetModel, targetModelFile);
				} catch (IOException e) {
					throw new RuntimeException("Failed to add " + id, e);
				}

				Model model = new Model(item, idx);
				item.models.put(id, model);
				models.put(id, model);
			} else throw new JsonSyntaxException(Messages.missingFieldAny(FIELD_INCLUDE, FIELD_ID));
		}
	}

	@Override
	protected Void createLocalData() {
		return null;
	}
}
