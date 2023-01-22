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
package multipacks.modifier.builtin.atlases;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import multipacks.utils.Messages;
import multipacks.utils.Selects;
import multipacks.utils.io.IOUtils;
import multipacks.vfs.Path;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class Template {
	public static final String FIELD_INCLUDE = "include";

	public static final String FIELD_SCALE = "scale";
	public static final String FIELD_PARTS = "parts";

	public static final int DEFAULT_SCALE = 1;

	public final int scale;
	public final Part[] parts;

	public Template(int scale, Part[] parts) {
		this.scale = scale;
		this.parts = parts;
	}

	public Template(JsonObject json) {
		this.scale = Selects.getChain(json.get(FIELD_SCALE), j -> j.getAsInt(), DEFAULT_SCALE);

		JsonArray partsJson = Selects.nonNull(json.get(FIELD_PARTS), Messages.missingFieldAny(FIELD_PARTS)).getAsJsonArray();
		this.parts = new Part[partsJson.size()];
		for (int i = 0; i < this.parts.length; i++) this.parts[i] = new Part(partsJson.get(i).getAsJsonObject());
	}

	public static Template resolveTemplate(Vfs scoped, Vfs configFile, JsonElement config) {
		if (configFile != null) configFile.getParent().delete(configFile.getName());

		if (config.isJsonObject()) {
			JsonObject obj = config.getAsJsonObject();

			if (obj.has(FIELD_INCLUDE)) {
				Path includePath = new Path(obj.get(FIELD_INCLUDE).getAsString());
				Vfs includeFile = scoped.get(includePath);
				if (includeFile == null) throw new RuntimeException(Messages.missingFile(includePath, scoped));

				try {
					JsonElement includeConfig = IOUtils.jsonFromVfs(includeFile);
					return resolveTemplate(scoped, includeFile, includeConfig);
				} catch (IOException e) {
					throw new RuntimeException("Failed to read config from " + includeFile, e);
				}
			} else return new Template(obj);
		} else throw new JsonSyntaxException("Not a JSON object: " + config);
	}
}
