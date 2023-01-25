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
package multipacks.packs.meta;

import com.google.gson.JsonObject;

import multipacks.utils.Messages;
import multipacks.utils.Selects;
import multipacks.versioning.Version;

/**
 * The pack identifier information.
 * @author nahkd
 *
 */
public class PackIdentifier {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_PACK_VERSION = "packVersion";

	public final String name;
	public final Version packVersion;

	public PackIdentifier(String name, Version version) {
		this.name = name;
		this.packVersion = version;
	}

	public PackIdentifier(JsonObject json) {
		this.name = Selects.nonNull(json.get(FIELD_NAME), Messages.missingFieldAny(FIELD_NAME)).getAsString();
		this.packVersion = new Version(Selects.nonNull(json.get(FIELD_PACK_VERSION), Messages.missingFieldAny(FIELD_PACK_VERSION)).getAsString());
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty(FIELD_NAME, name);
		json.addProperty(FIELD_PACK_VERSION, packVersion.toStringNoPrefix());
		return json;
	}

	@Override
	public String toString() {
		return "PackIdentifier [name=" + name + ", packVersion=" + packVersion + "]";
	}
}
