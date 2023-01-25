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
package multipacks.modifier.builtin;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.modifier.Modifier;
import multipacks.modifier.ModifiersAccess;
import multipacks.packs.Pack;
import multipacks.utils.Messages;
import multipacks.utils.Selects;
import multipacks.utils.io.IOUtils;
import multipacks.vfs.Path;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public abstract class BuiltinModifierBase<T> extends Modifier {
	public static final String FIELD_INCLUDE = "include";

	protected abstract void applyWithScopedConfig(Pack fromPack, Vfs root, Vfs scoped, JsonElement config, T data, ModifiersAccess access);
	protected abstract T createLocalData();

	private void applyWithScopedConfigPriv(Pack fromPack, Vfs root, Vfs scoped, JsonElement config, T data, ModifiersAccess access) {
		if (config.isJsonArray()) {
			JsonArray arr = config.getAsJsonArray();
			for (JsonElement e : arr) applyWithScopedConfigPriv(fromPack, root, scoped, e, data, access);
			return;
		} else if (config.isJsonObject()) {
			JsonObject obj = config.getAsJsonObject();
			if (obj.has(FIELD_INCLUDE)) {
				Path includePath = new Path(Selects.nonNull(obj.get(FIELD_INCLUDE), Messages.missingFieldAny(FIELD_INCLUDE)).getAsString());
				Vfs file = scoped.get(includePath);
				applyWithScopedConfig(fromPack, root, file.get(".."), file, data, access);
				return;
			}
		}

		applyWithScopedConfig(fromPack, root, scoped, config, data, access);
	}

	public void applyWithScopedConfig(Pack fromPack, Vfs root, Vfs scoped, Vfs configFile, T data, ModifiersAccess access) {
		try {
			JsonElement json = IOUtils.jsonFromVfs(configFile);
			applyWithScopedConfigPriv(fromPack, root, scoped, json, data, access);
			configFile.getParent().delete(configFile.getName());
		} catch (IOException e) {
			throw new RuntimeException("Failed to read config from " + configFile, e);
		}
	}

	@Override
	public void applyModifier(Pack fromPack, Vfs contents, JsonElement config, ModifiersAccess access) {
		applyWithScopedConfigPriv(fromPack, contents, contents, config, createLocalData(), access);
	}
}
