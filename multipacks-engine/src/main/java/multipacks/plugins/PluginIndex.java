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
package multipacks.plugins;

import com.google.gson.JsonObject;

import multipacks.utils.Messages;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class PluginIndex {
	public static final String FIELD_ID = "id";
	public static final String FIELD_MAIN = "main";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_AUTHOR = "author";

	public final ResourcePath id;
	public final String main;
	public String name, author;

	public PluginIndex(ResourcePath id, String main) {
		this.id = id;
		this.main = main;
	}

	public PluginIndex(JsonObject json) {
		this.id = new ResourcePath(Selects.nonNull(json.get(FIELD_ID), Messages.missingFieldAny(FIELD_ID)).getAsString());
		this.main = Selects.nonNull(json.get(FIELD_MAIN), Messages.missingFieldAny(FIELD_MAIN)).getAsString();
		this.name = Selects.getChain(json.get(FIELD_NAME), j -> j.getAsString(), null);
		this.author = Selects.getChain(json.get(FIELD_AUTHOR), j -> j.getAsString(), null);
	}
}
