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
package multipacks.cli;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import multipacks.logging.Logger;
import multipacks.modifier.Modifier;
import multipacks.platform.Platform;
import multipacks.platform.PlatformConfig;
import multipacks.plugins.Plugin;
import multipacks.repository.LocalRepository;
import multipacks.repository.Repository;
import multipacks.utils.ResourcePath;
import multipacks.utils.io.Deserializer;

public class CLIPlatform implements Platform {
	private Logger logger;
	private Map<ResourcePath, Supplier<Modifier>> modifierCtors = new HashMap<>();
	private Map<ResourcePath, Deserializer<Modifier>> modifierDeserializers = new HashMap<>();

	private Map<ResourcePath, Plugin> plugins = new HashMap<>();
	private List<Repository> repositories = new ArrayList<>();
	private LocalRepository installRepository;

	public CLIPlatform(Logger logger, PlatformConfig config, SystemEnum system) {
		this.logger = logger;
		config.collectRepositories(repo -> repositories.add(repo), system.getMultipacksDir());

		if (config.installRepository != null) {
			logger.debug("Install destination is {}", system.getMultipacksDir().resolve(config.installRepository).toString());
			installRepository = new LocalRepository(system.getMultipacksDir().resolve(config.installRepository));
		}
	}

	public void loadPlugin(ResourcePath id, Plugin plugin) {
		if (plugins.containsKey(id)) throw new IllegalArgumentException("Plugin is already loaded: " + id);

		try {
			plugin.onInit(this);
			Collection<Repository> repos = plugin.getPluginRepositories();
			if (repos != null) repositories.addAll(repos);
		} catch (Exception e) {
			throw new RuntimeException("Plugin load failed: " + id, e);
		}

		plugins.put(id, plugin);
	}

	public LocalRepository getInstallRepository() {
		return installRepository;
	}

	@Override
	public Collection<Repository> getRepositories() {
		return Collections.unmodifiableCollection(repositories);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public Modifier createModifier(ResourcePath id) {
		Supplier<Modifier> ctor = modifierCtors.get(id);
		if (ctor != null) return ctor.get();
		return null;
	}

	@Override
	public Modifier deserializeModifier(ResourcePath id, DataInput input) throws IOException {
		Deserializer<Modifier> deserializer = modifierDeserializers.get(id);
		if (deserializer != null) return deserializer.deserialize(input);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Modifier> void registerModifier(ResourcePath id, Supplier<T> supplier, Deserializer<T> deserializer) {
		if (modifierCtors.containsKey(id)) throw new IllegalArgumentException("Modifier is already registered: " + id);
		modifierCtors.put(id, (Supplier<Modifier>) supplier);
		modifierDeserializers.put(id, (Deserializer<Modifier>) deserializer);
	}

	@Override
	public List<ResourcePath> getRegisteredModifiers() {
		return new ArrayList<>(modifierCtors.keySet());
	}
}
