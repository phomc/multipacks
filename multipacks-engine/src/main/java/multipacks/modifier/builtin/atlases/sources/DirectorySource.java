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
package multipacks.modifier.builtin.atlases.sources;

import com.google.gson.JsonObject;

import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class DirectorySource extends AtlasSource {
	public static final String SOURCE_NAME = "directory";

	public static final String FIELD_SOURCE_DIR = "sourceDir";
	public static final String FIELD_PREFIX = "prefix";

	public final ResourcePath source;
	public final String prefix;

	public DirectorySource(ResourcePath source, String prefix) {
		super(SOURCE_NAME);
		this.source = source;
		this.prefix = prefix;
	}

	@Override
	public JsonObject toOutputSource() {
		JsonObject json = super.toOutputSource();
		json.addProperty("source", source.toString());
		json.addProperty("prefix", prefix);
		return json;
	}

	public static DirectorySource sourceFromConfig(JsonObject json) {
		ResourcePath source = new ResourcePath(Selects.nonNull(json.get(FIELD_SOURCE_DIR), Messages.missingFieldAny(FIELD_SOURCE_DIR)).getAsString());
		String prefix = Selects.nonNull(json.get(FIELD_PREFIX), Messages.missingFieldAny(FIELD_PREFIX)).getAsString();
		return new DirectorySource(source, prefix);
	}
}
