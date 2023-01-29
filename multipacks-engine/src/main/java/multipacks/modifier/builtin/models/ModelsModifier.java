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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import multipacks.modifier.ModifiersAccess;
import multipacks.modifier.builtin.BuiltinModifierBase;
import multipacks.modifier.builtin.models.overrides.CustomModelOverride;
import multipacks.modifier.builtin.models.overrides.ModelOverride;
import multipacks.packs.Pack;
import multipacks.utils.Constants;
import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.utils.io.IOUtils;
import multipacks.vfs.Path;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class ModelsModifier extends BuiltinModifierBase<Void> {
	public static final String ERROR_MISSING_MODEL_A = "Missing model JSON for ";

	public static final ResourcePath ID = new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/models");

	public static final String FIELD_TARGET = "target";
	public static final String FIELD_PREDICATE = "predicate";
	public static final String FIELD_MODEL = "model";
	public static final String FIELD_NAMED = "named";

	public static final ResourcePath DEFAULT_ITEM_ID = new ResourcePath(Constants.GAME_NAMESPACE, "barrier");

	public final Map<ResourcePath, BaseItemModel> items = new HashMap<>();
	public final Map<ResourcePath, ModelOverride> namedOverrides = new HashMap<>();

	@Override
	public void finalizeModifier(Vfs contents, ModifiersAccess access) {
		for (Map.Entry<ResourcePath, BaseItemModel> e : items.entrySet()) {
			Path targetModel = new Path("assets/" + e.getKey().namespace + "/models/item/" + e.getKey().path + ".json");
			JsonObject modelJson = e.getValue().toModelJson();

			try {
				IOUtils.jsonToVfs(modelJson, contents.touch(targetModel));
			} catch (IOException e1) {
				throw new RuntimeException("An error occured", e1);
			}
		}
	}

	@Override
	public void serializeModifier(DataOutput output, ModifiersAccess access) throws IOException {
	}

	public static ModelsModifier deserializeModifier(DataInput input) throws IOException {
		ModelsModifier mod = new ModelsModifier();
		return mod;
	}

	public static void registerTo(ModifiersAccess access) {
		access.registerModifier(ID, ModelsModifier::new, ModelsModifier::deserializeModifier);
	}

	@Override
	protected void applyWithScopedConfig(Pack fromPack, Vfs root, Vfs scoped, JsonElement config, Void data, ModifiersAccess access) {
		if (config.isJsonObject()) {
			JsonObject obj = config.getAsJsonObject();

			if (obj.has(FIELD_TARGET)) {
				ResourcePath targetId = new ResourcePath(Selects.nonNull(obj.get(FIELD_TARGET), Messages.missingFieldAny(FIELD_TARGET)).getAsString());
				ResourcePath modelId = new ResourcePath(Selects.nonNull(obj.get(FIELD_MODEL), Messages.missingFieldAny(FIELD_MODEL)).getAsString());
				ResourcePath namedOverride = Selects.getChain(obj.get(FIELD_NAMED), j -> new ResourcePath(j.getAsString()), null);
				String predicateType = Selects.nonNull(obj.get(FIELD_PREDICATE), Messages.missingFieldAny(FIELD_PREDICATE)).getAsString();

				Vfs targetModelFile = root.mkdir("assets").mkdir(targetId.namespace).mkdir("models").mkdir("item").get(targetId.path + ".json");

				// TODO: Obtain model from user's game installation (if exists)
				// We'll obtain it from ~/.minecraft/versions/<Version>/<version>.jar:assets/...
				// Or we can obtain it by downloading the JAR included inside version JSON file
				if (targetModelFile == null) throw new RuntimeException(ERROR_MISSING_MODEL_A + targetId.namespace + ":item/" + targetId.path);

				try {
					JsonObject targetModel = IOUtils.jsonFromVfs(targetModelFile).getAsJsonObject();
					BaseItemModel base = items.get(targetId);
					if (base == null) items.put(targetId, base = new BaseItemModel(targetId, targetModel));

					ModelOverride override;
					switch (predicateType) {
					case CustomModelOverride.PREDICATE_TYPE: override = base.allocateCustomModelId(modelId, namedOverride); break;
					default: throw new JsonSyntaxException("Unknown predicate type: " + predicateType);
					}

					if (namedOverride != null) namedOverrides.put(namedOverride, override);
				} catch (IOException e) {
					throw new RuntimeException("An error occured", e);
				}
			} else throw new JsonSyntaxException(Messages.missingFieldAny(FIELD_INCLUDE, FIELD_NAMED));
		}
	}

	@Override
	protected Void createLocalData() {
		return null;
	}
}
