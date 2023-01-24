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
package multipacks.spigot.platform;

import com.google.gson.JsonObject;

import multipacks.platform.PlatformConfig;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class SpigotPlatformConfig extends PlatformConfig {
	public static final String FIELD_MASTER_PACK = "masterPack";

	public String masterPack;

	public SpigotPlatformConfig() {
		super();
	}

	public SpigotPlatformConfig(JsonObject json) {
		super(json);
		masterPack = Selects.getChain(json.get(FIELD_MASTER_PACK), j -> j.getAsString(), null);
	}

	public static SpigotPlatformConfig createDefaultConfig() {
		SpigotPlatformConfig config = new SpigotPlatformConfig();
		config.repositories.put("local", "local ./repository");
		config.installRepository = "./repository";
		config.masterPack = "./master-pack";
		return config;
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = super.toJson();
		if (masterPack != null) json.addProperty(FIELD_MASTER_PACK, masterPack);
		return json;
	}
}
