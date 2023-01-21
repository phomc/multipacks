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
package multipacks.modifier;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.modifier.builtin.glyphs.GlyphsModifier;
import multipacks.modifier.builtin.models.ModelsModifier;
import multipacks.packs.Pack;
import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public abstract class Modifier {
	public static final String FIELD_ID = "type";
	public static final String FIELD_CONFIG = "config";

	/**
	 * Apply this modifier to VFS.
	 * @param fromPack Pack that configured this modifier.
	 * @param contents Input and output contents.
	 * @param config Modifier configuration.
	 * @param access Interface for accessing registered modifiers.
	 */
	public abstract void applyModifier(Pack fromPack, Vfs contents, JsonElement config, ModifiersAccess access);

	public abstract void finalizeModifier(Vfs contents, ModifiersAccess access);

	/**
	 * Serialize this modifier.
	 * @param output Output data stream, which you can write modifier outputs there.
	 * @param access Interface for accessing registered modifiers.
	 */
	public void serializeModifier(DataOutput output, ModifiersAccess access) throws IOException {
		// NO-OP
	}

	public static void applyModifiers(Pack fromPack, Vfs contents, JsonArray list, ModifiersAccess access, Map<ResourcePath, Modifier> modifiersMap) {
		for (JsonElement e : list) {
			JsonObject obj = e.getAsJsonObject();
			ResourcePath id = new ResourcePath(Selects.nonNull(obj.get(FIELD_ID), Messages.missingFieldAny(FIELD_ID)).getAsString());
			JsonElement config = obj.get(FIELD_CONFIG);

			Modifier modifier = null;
			if (modifiersMap != null) modifier = modifiersMap.get(id);

			if (modifier == null) {
				modifier = access.createModifier(id);
				if (modifiersMap != null) modifiersMap.put(id, modifier);
			}

			if (modifier == null) throw new NullPointerException("Modifier with type id = " + id + " not found!");
			modifier.applyModifier(fromPack, contents, config, access);
		}
	}

	public static void registerBuiltinModifiers(ModifiersAccess access) {
		GlyphsModifier.registerTo(access);
		ModelsModifier.registerTo(access);
	}
}
