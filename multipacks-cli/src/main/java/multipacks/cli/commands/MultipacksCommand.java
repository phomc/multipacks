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
package multipacks.cli.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Option;
import multipacks.cli.api.annotations.Subcommand;
import multipacks.cli.commands.management.RemoteCommand;
import multipacks.plugins.Plugin;
import multipacks.plugins.PluginIndex;
import multipacks.utils.io.IOUtils;

public class MultipacksCommand extends Command {
	public final CLIPlatform platform;
	private List<URLClassLoader> openedClassLoaders = new ArrayList<>();

	@Subcommand("search") public final SearchCommand search;
	@Subcommand("info") public final InfoCommand info;
	@Subcommand("build") public final BuildCommand build;
	@Subcommand("install") public final InstallCommand install;
	@Subcommand("uninstall") public final UninstallCommand uninstall;
	@Subcommand("download") public final DownloadCommand download;
	@Subcommand("remote") public final RemoteCommand remote;
	@Subcommand("include") public final IncludeCommand include;

	public MultipacksCommand(CLIPlatform platform) {
		this.platform = platform;

		this.search = new SearchCommand(this);
		this.info = new InfoCommand(this);
		this.build = new BuildCommand(this);
		this.install = new InstallCommand(this);
		this.uninstall = new UninstallCommand(this);
		this.download = new DownloadCommand(this);
		this.remote = new RemoteCommand(this);
		this.include = new IncludeCommand(this);

		helpName = "java multipacks.cli.Main";
		helpDescription = "Multipacks CLI";
	}

	@Option(value = { "--plugin", "-P" }, helpDescription = "Load Multipacks plugin from JAR file")
	public void loadPlugin(String pathToPlugin) throws URISyntaxException, IOException {
		Path pluginPath = new File(pathToPlugin).toPath();
		URL url = pluginPath.toUri().toURL();

		URLClassLoader clsLoader = new URLClassLoader(new URL[] { url }, getClass().getClassLoader());
		URL indexUrl = clsLoader.getResource(Plugin.INDEX_FILENAME);
		if (indexUrl == null) {
			clsLoader.close();
			throw new CommandException("Plugin '" + pathToPlugin + "' does not have " + Plugin.INDEX_FILENAME);
		}

		try (InputStream stream = clsLoader.getResourceAsStream(Plugin.INDEX_FILENAME)) {
			PluginIndex index = new PluginIndex(IOUtils.jsonFromStream(stream).getAsJsonObject());

			platform.getLogger().info("Loading {}", index.name + (index.author != null? (" by " + index.author) : ""));
			try {
				Class<?> cls = clsLoader.loadClass(index.main);
				if (!Plugin.class.isAssignableFrom(cls)) throw new ClassCastException("Main class " + index.main + " does not extends " + Plugin.class.getCanonicalName());
				Plugin plugin = (Plugin) cls.getConstructor().newInstance();
				platform.loadPlugin(index.id, plugin);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Main class not found: " + index.main + ". Please check your plugin's " + Plugin.INDEX_FILENAME, e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Plugin construction failed (Is it an abstract class?)", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Exception thrown inside constructor", e.getCause());
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("No constructor in main class " + index.main + " with the signature <init>(). Please add one.", e);
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("An error occured", e);
			} finally {
				openedClassLoaders.add(clsLoader);
			}
		}
	}

	@Override
	protected void postExecute() throws CommandException {
		for (URLClassLoader clsLoader : openedClassLoaders) {
			try {
				clsLoader.close();
			} catch (IOException e) {
				// TODO: Keep closing other class loaders
				throw new CommandException("Error while closing class loader", e);
			}
		}
	}

	@Override
	protected void onExecuteWithoutSubcommand() throws CommandException {
		printHelp(System.out);
	}
}
