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
package multipacks.cli;

import com.google.gson.JsonObject;

import multipacks.platform.PlatformConfig;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class CLIPlatformConfig extends PlatformConfig {
	public static final String FIELD_GAME_DIR = "gameDir";

	public String gameDir;

	public CLIPlatformConfig(JsonObject json) {
		super(json);
		this.gameDir = Selects.getChain(json.get(FIELD_GAME_DIR), j -> j.getAsString(), null);
	}

	public CLIPlatformConfig() {
	}

	@Override
	public PlatformConfig defaultConfig() {
		super.defaultConfig();
		gameDir = SystemEnum.getPlatform().getMinecraftDir().toAbsolutePath().toString();
		return this;
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = super.toJson();
		if (gameDir != null) json.addProperty(FIELD_GAME_DIR, gameDir);
		return json;
	}
}
