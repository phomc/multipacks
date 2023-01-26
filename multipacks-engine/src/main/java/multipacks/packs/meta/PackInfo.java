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
package multipacks.packs.meta;

import com.google.gson.JsonObject;

import multipacks.utils.Selects;
import multipacks.versioning.Version;

/**
 * @author nahkd
 *
 */
public class PackInfo extends PackIdentifier {
	public static final String FIELD_AUTHOR = "author";
	public static final String FIELD_SOURCE_GAME_VERSION = "sourceGameVersion";

	public final String author;
	public final Version sourceGameVersion;

	/**
	 * Create new pack metadata.
	 * @param name Name of this pack. Only {@code [a-z0-9\-_]} characters are allowed.
	 * @param packVersion Version of this pack.
	 * @param author Name of author that made this pack.
	 * @param sourceGameVersion Game version that the sources originally targeted.
	 */
	public PackInfo(String name, Version packVersion, String author, Version sourceGameVersion) {
		super(name, packVersion);
		this.author = author;
		this.sourceGameVersion = sourceGameVersion;
	}

	public PackInfo(String name, Version packVersion) {
		super(name, packVersion);
		this.author = null;
		this.sourceGameVersion = null;
	}

	public PackInfo(JsonObject json) {
		super(json);
		author = Selects.getChain(json.get(FIELD_AUTHOR), j -> j.getAsString(), null);
		sourceGameVersion = Selects.getChain(json.get(FIELD_SOURCE_GAME_VERSION), j -> new Version(j.getAsString()), null);
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = super.toJson();
		if (author != null) json.addProperty(FIELD_AUTHOR, author);
		if (sourceGameVersion != null) json.addProperty(FIELD_SOURCE_GAME_VERSION, sourceGameVersion.toString());
		return json;
	}
}
