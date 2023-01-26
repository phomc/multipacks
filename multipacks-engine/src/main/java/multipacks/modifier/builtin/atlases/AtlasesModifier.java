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
package multipacks.modifier.builtin.atlases;

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
import multipacks.modifier.builtin.atlases.sources.AtlasSource;
import multipacks.packs.Pack;
import multipacks.utils.Constants;
import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.utils.io.IOUtils;
import multipacks.vfs.Vfs;

/**
 * <b>Note:</b> This is not something like "cutting from image and export that area to another image file". This one
 * is about atlases generation (generate atlases inside {@code assets/namespace/atlases}).<br> 
 * @author nahkd
 *
 */
public class AtlasesModifier extends BuiltinModifierBase<Void> {
	public static final ResourcePath ID = new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/atlases");

	public static final String FIELD_ATLAS = "atlas";
	public static final String FIELD_SOURCE_TYPE = "sourceType";

	public final Map<ResourcePath, Atlas> atlases = new HashMap<>();

	@Override
	protected void applyWithScopedConfig(Pack fromPack, Vfs root, Vfs scoped, JsonElement config, Void data, ModifiersAccess access) {
		if (config.isJsonObject()) {
			JsonObject obj = config.getAsJsonObject();

			if (obj.has(FIELD_ATLAS)) {
				ResourcePath atlasId = new ResourcePath(obj.get(FIELD_ATLAS).getAsString());
				Atlas atlas = atlases.get(atlasId);
				if (atlas == null) atlases.put(atlasId, atlas = new Atlas(atlasId));

				String sourceType = Selects.nonNull(obj.get(FIELD_SOURCE_TYPE), Messages.missingFieldAny(FIELD_SOURCE_TYPE)).getAsString();
				AtlasSource source = AtlasSource.sourceFromConfig(sourceType, obj);
				if (source == null) throw new JsonSyntaxException("Unknown atlas source type: " + sourceType);

				atlas.sources.add(source);
			} else throw new JsonSyntaxException(Messages.missingFieldAny(FIELD_INCLUDE, FIELD_ATLAS));
		}
	}

	@Override
	protected Void createLocalData() {
		return null;
	}

	@Override
	public void finalizeModifier(Vfs contents, ModifiersAccess access) {
		for (Atlas atlas : atlases.values()) {
			Vfs atlasFile = contents.touch(atlas.getTargetPath());
			try {
				IOUtils.jsonToVfs(atlas.toOutput(), atlasFile);
			} catch (IOException e) {
				throw new RuntimeException("An error occured while writing atlas '" + atlas.id + "'", e);
			}
		}
	}

	@Override
	public void serializeModifier(DataOutput output, ModifiersAccess access) throws IOException {
	}

	public static AtlasesModifier deserializeModifier(DataInput input) throws IOException {
		return new AtlasesModifier();
	}

	public static void registerTo(ModifiersAccess access) {
		access.registerModifier(ID, AtlasesModifier::new, AtlasesModifier::deserializeModifier);
	}
}
