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

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import multipacks.bundling.BundleContext;
import multipacks.modifier.Modifier;
import multipacks.modifier.ModifiersAccess;
import multipacks.modifier.builtin.models.overrides.CustomModelOverride;
import multipacks.modifier.builtin.models.overrides.ModelOverride;
import multipacks.modifier.builtin.models.overrides.TrimModelOverride;
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
public class ModelsModifier extends Modifier<multipacks.modifier.builtin.models.ModelsModifier.Config, Void> {
	public static class Config {
		public ResourcePath target;
		public ResourcePath model;
		public ResourcePath named;
		public String predicateType;

		public ModelOverride overrideResult;
	}

	public static final String ERROR_MISSING_MODEL_A = "Missing model JSON for ";
	public static final String ERROR_MISSING_MODEL_B = ". If you are using Multipacks CLI, you can use 'multipacks-cli include <path/to/model.json>'";

	public static final ResourcePath ID = new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/models");

	public static final String FIELD_TARGET = "target";
	public static final String FIELD_PREDICATE = "predicate";
	public static final String FIELD_MODEL = "model";
	public static final String FIELD_NAMED = "named";

	public static final ResourcePath DEFAULT_ITEM_ID = new ResourcePath(Constants.GAME_NAMESPACE, "barrier");

	public final Map<ResourcePath, BaseItemModel> items = new HashMap<>();
	public final Map<ResourcePath, ModelOverride> namedOverrides = new HashMap<>();

	@Override
	public Config configure(JsonObject json) {
		Config config = new Config();

		if (json.has(FIELD_TARGET)) {
			config.target = new ResourcePath(Selects.nonNull(json.get(FIELD_TARGET), Messages.missingFieldAny(FIELD_TARGET)).getAsString());
			config.model = new ResourcePath(Selects.nonNull(json.get(FIELD_MODEL), Messages.missingFieldAny(FIELD_MODEL)).getAsString());
			config.named = Selects.getChain(json.get(FIELD_NAMED), j -> new ResourcePath(j.getAsString()), null);
			config.predicateType = Selects.nonNull(json.get(FIELD_PREDICATE), Messages.missingFieldAny(FIELD_PREDICATE)).getAsString();
		} else throw new JsonSyntaxException(Messages.missingFieldAny(FIELD_INCLUDE, FIELD_NAMED));

		return config;
	}

	@Override
	public Void createContext() {
		return null;
	}

	@Override
	public void applyModifier(BundleContext context, Path cwd, Config config, Void modContext) {
		Vfs targetModelFile = context.content.mkdir("assets").mkdir(config.target.namespace).mkdir("models").mkdir("item").get(config.target.path + ".json");
		if (targetModelFile == null) throw new RuntimeException(ERROR_MISSING_MODEL_A + config.target.namespace + ":item/" + config.target.path + ERROR_MISSING_MODEL_B);

		try {
			JsonObject targetModel = IOUtils.jsonFromVfs(targetModelFile).getAsJsonObject();
			BaseItemModel base = items.get(config.target);
			if (base == null) items.put(config.target, base = new BaseItemModel(config.target, targetModel));

			ModelOverride override;
			switch (config.predicateType) {
			case CustomModelOverride.PREDICATE_TYPE: override = base.allocateCustomModelId(config.model, config.named); break;
			case TrimModelOverride.PREDICATE_TYPE: override = base.allocateTrim(config.model, config.named); break;
			default: throw new JsonSyntaxException("Unknown predicate type: " + config.predicateType);
			}

			if (config.named != null) namedOverrides.put(config.named, override);
			config.overrideResult = override;
		} catch (IOException e) {
			throw new RuntimeException("An error occured", e);
		}
	}

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
}
