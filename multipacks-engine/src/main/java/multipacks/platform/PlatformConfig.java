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
package multipacks.platform;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import multipacks.repository.LocalRepository;
import multipacks.repository.Repository;
import multipacks.utils.Selects;

/**
 * @author nahkd
 *
 */
public class PlatformConfig {
	public static final String FILENAME = "multipacks.config.json";

	public static final String FIELD_REPOSITORIES = "repositories";
	public static final String FIELD_INSTALL_REPOSITORY = "installRepository";

	public final Map<String, String> repositories = new HashMap<>();
	public String installRepository;

	public PlatformConfig(JsonObject json) {
		JsonObject repoJson = Selects.getChain(json.get(FIELD_REPOSITORIES), j -> j.getAsJsonObject(), new JsonObject());
		for (Map.Entry<String, JsonElement> e : repoJson.entrySet()) repositories.put(e.getKey(), e.getValue().getAsString());

		installRepository = Selects.getChain(json.get(FIELD_INSTALL_REPOSITORY), j -> j.getAsString(), null);
	}

	public PlatformConfig() {
	}

	public PlatformConfig defaultConfig() {
		repositories.put("local", "local ./repository");
		installRepository = "./repository";
		return this;
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();

		JsonObject repoJson = new JsonObject();
		for (Map.Entry<String, String> e : repositories.entrySet()) repoJson.addProperty(e.getKey(), e.getValue());
		json.add(FIELD_REPOSITORIES, repoJson);

		if (installRepository != null) json.addProperty(FIELD_INSTALL_REPOSITORY, installRepository);
		return json;
	}

	public void collectRepositories(Consumer<Repository> collector, Path cwd) {
		for (Map.Entry<String, String> e : repositories.entrySet()) {
			collector.accept(Repository.fromConnectionString(e.getValue(), cwd));
		}
	}

	public LocalRepository getInstallRepository() {
		if (installRepository == null) return null;
		return new LocalRepository(new File(installRepository).toPath());
	}
}
