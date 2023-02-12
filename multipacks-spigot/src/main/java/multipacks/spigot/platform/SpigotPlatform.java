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
package multipacks.spigot.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multipacks.bundling.BundleResult;
import multipacks.bundling.Bundler;
import multipacks.logging.Logger;
import multipacks.modifier.Modifier;
import multipacks.modifier.ModifierInfo;
import multipacks.packs.LocalPack;
import multipacks.packs.meta.PackIndex;
import multipacks.platform.Platform;
import multipacks.platform.PlatformConfig;
import multipacks.plugins.Plugin;
import multipacks.repository.Repository;
import multipacks.spigot.MultipacksSpigot;
import multipacks.utils.PlatformAPI;
import multipacks.utils.ResourcePath;
import multipacks.utils.io.IOUtils;
import multipacks.versioning.Version;

public class SpigotPlatform implements Platform {
	private MultipacksSpigot plugin;

	private SpigotLogger logger;
	private Map<ResourcePath, ModifierInfo<?, ?, ?>> modifiers = new HashMap<>();

	private Map<ResourcePath, Plugin> plugins = new HashMap<>();
	private List<Repository> repositories = new ArrayList<>();
	private LocalPack masterPack;
	private BundleResult masterBuildOutput;

	public SpigotPlatform(MultipacksSpigot plugin) {
		this.plugin = plugin;
		this.logger = new SpigotLogger(plugin.getLogger());
	}

	@PlatformAPI
	public void loadConfig() throws IOException {
		repositories.clear();
		modifiers.clear();

		logger.debug("Loading Multipack plugins...");
		for (Plugin p : plugins.values()) {
			try {
				p.onInit(this);

				Collection<Repository> repos = p.getPluginRepositories();
				if (repos != null) repositories.addAll(repos);
			} catch (Exception e) {
				logger.error("Error while loading plugin:");
				e.printStackTrace();
			}
		}

		Path configFile = getMultipacksDir().resolve(PlatformConfig.FILENAME);

		if (Files.notExists(configFile)) {
			logger.warning("File {} does not exists (searching {}), creating new file...", PlatformConfig.FILENAME, configFile);
			Files.createDirectories(configFile.getParent());

			SpigotPlatformConfig config = new SpigotPlatformConfig().defaultConfig();
			IOUtils.jsonToFile(config.toJson(), configFile.toFile());

			Files.createDirectories(getMultipacksDir().resolve(config.installRepository));
			Files.createDirectories(getMultipacksDir().resolve(config.masterPack));

			// Master pack init
			PackIndex index = new PackIndex("master", new Version("1.0.0"), "Multipacks for Spigot", MultipacksSpigot.detectGameVersion(), "Declare which packs you want use in 'dependencies' field");
			IOUtils.jsonToFile(index.toJson(), getMultipacksDir().resolve(config.masterPack).resolve(LocalPack.FILE_INDEX).toFile());
		}

		logger.info("Loading config from {}", PlatformConfig.FILENAME);
		SpigotPlatformConfig config = new SpigotPlatformConfig(IOUtils.jsonFromPath(configFile).getAsJsonObject());
		config.collectRepositories(repo -> repositories.add(repo), getMultipacksDir());

		if (config.masterPack != null) {
			logger.info("Master pack declared in configuration file: {}", config.masterPack);
			masterPack = new LocalPack(getMultipacksDir().resolve(config.masterPack));
			masterPack.loadFromStorage();
		} else {
			masterPack = null;
		}

		masterBuildOutput = null;
		if (masterPack != null && config.prebuild) getMasterBuildOutput();
	}

	@PlatformAPI
	public LocalPack getMasterPack() {
		return masterPack;
	}

	@PlatformAPI
	public BundleResult getMasterBuildOutput() {
		if (masterPack == null) return null;

		if (masterBuildOutput == null) {
			logger.info("Building master pack...");
			long nano = System.nanoTime();

			Bundler bundler = new Bundler().fromPlatform(this);
			masterBuildOutput = bundler.bundle(masterPack, MultipacksSpigot.detectGameVersion());

			logger.info("Master pack built in {}ms", (System.nanoTime() - nano) * Math.pow(10, -6));
		}

		return masterBuildOutput;
	}

	@PlatformAPI
	public void addPlugin(ResourcePath id, Plugin plugin) {
		if (plugins.containsKey(id)) throw new IllegalArgumentException("Plugin is already added: " + id);
		plugins.put(id, plugin);
	}

	@PlatformAPI
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
	public <C, X, T extends Modifier<C, X>> void registerModifier(ResourcePath id, ModifierInfo<C, X, T> info) {
		if (modifiers.containsKey(id)) throw new IllegalArgumentException("Modifier is already registered: " + id);
		modifiers.put(id, info);
	}

	@Override
	public List<ResourcePath> getRegisteredModifiers() {
		return new ArrayList<>(modifiers.keySet());
	}

	@Override
	public ModifierInfo<?, ?, ?> getModifierInfo(ResourcePath id) {
		return modifiers.get(id);
	}
}
