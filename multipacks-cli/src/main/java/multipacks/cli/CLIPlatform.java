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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multipacks.logging.Logger;
import multipacks.modifier.Modifier;
import multipacks.modifier.ModifierInfo;
import multipacks.platform.Platform;
import multipacks.plugins.Plugin;
import multipacks.repository.LocalRepository;
import multipacks.repository.Repository;
import multipacks.utils.ResourcePath;
import multipacks.versioning.Version;
import multipacks.vfs.Vfs;

public class CLIPlatform implements Platform {
	private Logger logger;
	private Map<ResourcePath, ModifierInfo<?, ?, ?>> modifiers = new HashMap<>();

	private Map<ResourcePath, Plugin> plugins = new HashMap<>();
	private List<Repository> repositories = new ArrayList<>();
	private LocalRepository installRepository;
	private Path gameDir;

	public CLIPlatform(Logger logger, CLIPlatformConfig config, SystemEnum system) {
		this.logger = logger;
		config.collectRepositories(repo -> repositories.add(repo), system.getMultipacksDir());

		if (config.installRepository != null) {
			logger.debug("Install destination is {}", system.getMultipacksDir().resolve(config.installRepository).toString());
			installRepository = new LocalRepository(system.getMultipacksDir().resolve(config.installRepository));
		}

		if (config.gameDir != null) {
			logger.debug("Game directory is {}", config.gameDir);
			gameDir = new File(config.gameDir).toPath();
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

	public Path getGameJar(Version version) {
		if (gameDir == null) throw new IllegalStateException("Game installation folder is not defined in .multipacks/multipacks.config.json");
		return gameDir.resolve("versions").resolve(version.toStringNoPrefix()).resolve(version.toStringNoPrefix() + ".jar");
	}

	public void getGameJarFile(Vfs output, multipacks.vfs.Path path) {
		Path jar = getGameJar(new Version("1.19.3"));
		String[] segments = path.getSegments();

		try (FileSystem fs = FileSystems.newFileSystem(jar)) {
			Path current = fs.getPath(segments[0]);
			for (int i = 1; i < segments.length; i++) current = current.resolve(segments[i]);

			Vfs file = output.touch(path);
			try (InputStream inStream = Files.newInputStream(current)) {
				try (OutputStream outStream = file.getOutputStream()) {
					inStream.transferTo(outStream);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("An error occured", e);
		}
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
