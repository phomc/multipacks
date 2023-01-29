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
package multipacks.modifier;

import java.io.DataOutput;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleContext;
import multipacks.modifier.builtin.atlases.AtlasesModifier;
import multipacks.modifier.builtin.glyphs.GlyphsModifier;
import multipacks.modifier.builtin.models.ModelsModifier;
import multipacks.modifier.builtin.slices.SlicesModifier;
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
public abstract class Modifier<C, X> {
	public static final String FIELD_ID = "type";
	public static final String FIELD_CONFIG = "config";
	public static final String FIELD_INCLUDE = "include";

	public abstract C configure(JsonObject json);
	public abstract X createContext();
	public abstract void applyModifier(BundleContext context, Path cwd, C config, X modContext);
	public abstract void finalizeModifier(Vfs contents, ModifiersAccess access);

	/**
	 * Serialize this modifier.
	 * @param output Output data stream, which you can write modifier outputs there.
	 * @param access Interface for accessing registered modifiers.
	 */
	public void serializeModifier(DataOutput output, ModifiersAccess access) throws IOException {
		// NO-OP
	}

	@SuppressWarnings("unchecked")
	public static void applyModifiers(BundleContext context, JsonArray list) {
		for (JsonElement e : list) {
			JsonObject obj = e.getAsJsonObject();
			ResourcePath id = new ResourcePath(Selects.nonNull(obj.get(FIELD_ID), Messages.missingFieldAny(FIELD_ID)).getAsString());
			JsonElement config = obj.get(FIELD_CONFIG);

			Modifier<?, ?> modifier = context.getOrCreateModifier(id);
			if (modifier == null) throw new NullPointerException("Modifier with type id = " + id + " not found!");
			Object modContext = modifier.createContext();

			walkModifierConfigs(context, (Modifier<Object, Object>) modifier, modContext, Path.ROOT, config);
		}
	}

	private static <C, X> void walkModifierConfigs(BundleContext context, Modifier<C, X> mod, X modContext, Path cwd, JsonElement json) {
		if (json.isJsonArray()) {
			JsonArray arr = json.getAsJsonArray();
			for (JsonElement e : arr) walkModifierConfigs(context, mod, modContext, cwd, e);
		} else if (json.isJsonObject()) {
			JsonObject obj = json.getAsJsonObject();

			if (obj.has(FIELD_INCLUDE)) {
				String includeStr = obj.get(FIELD_INCLUDE).getAsString();
				Path next = cwd.join(includeStr);
				Path nextCwd = next.join("..");

				Vfs file = context.content.get(next);
				try {
					JsonElement nextJson = IOUtils.jsonFromVfs(file);
					walkModifierConfigs(context, mod, modContext, nextCwd, nextJson);
					file.getParent().delete(file.getName());
				} catch (IOException e) {
					throw new RuntimeException("An error occured while including " + next, e);
				}
			} else {
				mod.applyModifier(context, cwd, mod.configure(obj), modContext);
			}
		}
	}

	public static void registerBuiltinModifiers(ModifiersAccess access) {
		GlyphsModifier.registerTo(access);
		ModelsModifier.registerTo(access);
		SlicesModifier.registerTo(access);
		AtlasesModifier.registerTo(access);
	}
}
