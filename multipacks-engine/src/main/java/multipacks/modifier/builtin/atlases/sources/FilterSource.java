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

import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class FilterSource extends AtlasSource {
	public static final String SOURCE_NAME = "filter";

	public static final String FIELD_NAMESPACE_PATTERN = "namespacePattern";
	public static final String FIELD_PATH_PATTERN = "pathPattern";

	public final String namespacePattern;
	public final String pathPattern;

	public FilterSource(String namespacePattern, String pathPattern) {
		super(SOURCE_NAME);
		this.namespacePattern = namespacePattern;
		this.pathPattern = pathPattern;
	}

	@Override
	public JsonObject toOutputSource() {
		JsonObject json = super.toOutputSource();
		if (namespacePattern != null) json.addProperty("namespace", namespacePattern);
		if (pathPattern != null) json.addProperty("path", pathPattern);
		return json;
	}

	public static FilterSource sourceFromConfig(JsonObject json) {
		String namespacePattern = Selects.getChain(json.get(FIELD_NAMESPACE_PATTERN), j -> j.getAsString(), null);
		String pathPattern = Selects.getChain(json.get(FIELD_PATH_PATTERN), j -> j.getAsString(), null);
		return new FilterSource(namespacePattern, pathPattern);
	}
}
