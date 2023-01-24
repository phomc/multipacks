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

import java.io.DataInput;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import multipacks.bundling.BundleResult;
import multipacks.bundling.Bundler;
import multipacks.logging.Logger;
import multipacks.modifier.Modifier;
import multipacks.packs.LocalPack;
import multipacks.packs.meta.PackIndex;
import multipacks.platform.Platform;
import multipacks.platform.PlatformConfig;
import multipacks.plugins.Plugin;
import multipacks.repository.Repository;
import multipacks.spigot.MultipacksSpigot;
import multipacks.utils.ResourcePath;
import multipacks.utils.io.Deserializer;
import multipacks.utils.io.IOUtils;
import multipacks.versioning.Version;

public class SpigotPlatform implements Platform {
	private MultipacksSpigot plugin;

	private SpigotLogger logger;
	private Map<ResourcePath, Supplier<Modifier>> modifierCtors = new HashMap<>();
	private Map<ResourcePath, Deserializer<Modifier>> modifierDeserializers = new HashMap<>();

	private Map<ResourcePath, Plugin> plugins = new HashMap<>();
	private boolean pluginsLoadFinalized = false;

	private List<Repository> repositories = new ArrayList<>();
	private LocalPack masterPack;
	private BundleResult masterBuildOutput;

	public SpigotPlatform(MultipacksSpigot plugin) {
		this.plugin = plugin;
		this.logger = new SpigotLogger(plugin.getLogger());

		try {
			reloadConfig();
		} catch (IOException e) {
			throw new RuntimeException("An error occured while loading config", e);
		}
	}

	public void reloadConfig() throws IOException {
		Path configFile = getMultipacksDir().resolve(PlatformConfig.FILENAME);
		if (Files.notExists(configFile)) {
			logger.warning("File {} does not exists (searching {}), creating new file...", PlatformConfig.FILENAME, configFile);
			SpigotPlatformConfig config = SpigotPlatformConfig.createDefaultConfig();
			IOUtils.jsonToFile(config.toJson(), configFile.toFile());

			Files.createDirectories(getMultipacksDir().resolve(config.installRepository));
			Files.createDirectories(getMultipacksDir().resolve(config.masterPack));

			// Master pack init
			PackIndex index = new PackIndex("master", new Version("1.0.0"), "Multipacks for Spigot", new Version("1.19"), "Declare which packs you want use in 'dependencies' field");
			IOUtils.jsonToFile(index.toJson(), getMultipacksDir().resolve(config.masterPack).resolve(LocalPack.FILE_INDEX).toFile());
		}

		logger.info("Loading config from {}", PlatformConfig.FILENAME);
		SpigotPlatformConfig config = new SpigotPlatformConfig(IOUtils.jsonFromPath(configFile).getAsJsonObject());
		config.collectRepositories(repo -> repositories.add(repo), getMultipacksDir());

		if (config.masterPack != null) {
			logger.info("Master pack declared in configuration file: {}", config.masterPack);
			masterPack = new LocalPack(getMultipacksDir().resolve(config.masterPack));
		} else {
			masterPack = null;
		}

		masterBuildOutput = null;
	}

	public LocalPack getMasterPack() {
		return masterPack;
	}

	public BundleResult getMasterBuildOutput() {
		if (masterPack == null) return null;

		if (masterBuildOutput == null) {
			logger.info("Building master pack...");
			long nano = System.nanoTime();

			Bundler bundler = new Bundler().fromPlatform(this);
			masterBuildOutput = bundler.bundle(masterPack, new Version("1.19") /* TODO: game version */);

			logger.info("Master pack built in {}ms", (System.nanoTime() - nano) * Math.pow(10, -6));
		}

		return masterBuildOutput;
	}

	public void loadPlugin(ResourcePath id, Plugin plugin) {
		if (pluginsLoadFinalized) throw new IllegalStateException("Plugins registration is closed");
		if (plugins.containsKey(id)) throw new IllegalArgumentException("Plugin is already loaded: " + id);

		try {
			plugin.onInit(this);
		} catch (Exception e) {
			throw new RuntimeException("Plugin load failed: " + id, e);
		}

		plugins.put(id, plugin);
	}

	public void finalizePluginsLoad() {
		if (pluginsLoadFinalized) return;
		pluginsLoadFinalized = true;

		for (Plugin p : plugins.values()) {
			Collection<Repository> repos = p.getPluginRepositories();
			if (repos != null) repositories.addAll(repos);
		}
	}

	public Path getMultipacksDir() {
		return plugin.getDataFolder().toPath();
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
