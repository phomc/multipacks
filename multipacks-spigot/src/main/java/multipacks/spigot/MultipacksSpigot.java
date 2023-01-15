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
package multipacks.spigot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleIgnore;
import multipacks.bundling.BundleInclude;
import multipacks.bundling.BundleResult;
import multipacks.bundling.PackBundler;
import multipacks.management.legacy.PacksRepository;
import multipacks.packs.Pack;
import multipacks.plugins.MultipacksDefaultPlugin;
import multipacks.plugins.MultipacksPlugin;
import multipacks.spigot.commands.MainCommand;
import multipacks.spigot.serving.LocalPackServer;
import multipacks.spigot.serving.PackServer;
import multipacks.utils.IOUtils;
import multipacks.utils.PlatformAPI;
import multipacks.utils.ResourcePath;
import multipacks.utils.Selects;
import multipacks.utils.logging.AbstractMPLogger;

/**
 * The entry point for accessing Multipacks API for Spigot. Contains some methods for converting between Multipacks
 * objects and Spigot/Bukkit objects, as well as packs generation.
 * @author nahkd
 * @see #getInstance()
 * @see #getBuildOutput()
 * @apiNote While using Multipacks Spigot, please do not touch any method related to Multipacks dynamic plugins
 * APIs (those are {@link MultipacksPlugin#loadJarPlugin(File)} and {@link MultipacksPlugin#loadJarPlugin(java.net.URL)}).
 * Some users may use /reload (not recommended, but who cares?) or "plugins management" plugin to controls how server
 * plugins works. Using any of these methods will causes memory leak. The only exception is
 * {@link MultipacksPlugin#loadPlugin(MultipacksPlugin)}, which loads the plugin instance.<br>
 * 
 * It is also important to always load Multipacks plugin on {@link Plugin#onLoad()} method, or your post processing
 * passes will not works.
 *
 */
@PlatformAPI
public class MultipacksSpigot extends JavaPlugin {
	private JavaMPLogger logger;
	private JsonObject config;
	private List<PacksRepository> repos;
	private PacksRepository selectedRepo;

	private Pack masterPack;
	private File packArtifact;
	private BundleResult packOutput;
	private PackServer packServer;

	// Callbacks
	private HashSet<Consumer<BundleResult>> onMasterRebuild = new HashSet<>();

	private static MultipacksSpigot INSTANCE;

	@Override
	public void onLoad() {
		MultipacksPlugin.loadPlugin(new MultipacksDefaultPlugin());
		PackServer.BUILDERS.put("local", LocalPackServer::new);
	}

	@Override
	public void onEnable() {
		INSTANCE = this;
		long benchmarkStart = System.nanoTime();

		logger = new JavaMPLogger(getLogger());
		reloadJsonConfig(true);

		getCommand("multipacks").setExecutor(new MainCommand(this));
		logger.info("Plugin enabled in " + new DecimalFormat("#0.000").format(((System.nanoTime() - benchmarkStart) / 1000000.0)) + "ms");
	}

	/**
	 * Reload plugin configurations.
	 */
	public void reloadJsonConfig(boolean enablingPlugin) {
		// Reset all
		config = null;
		repos = null;
		selectedRepo = null;

		masterPack = null;
		packArtifact = null;
		packOutput = null;

		File configFile = new File(getDataFolder(), "config.json");

		if (!configFile.exists()) {
			logger.warning("config.json doesn't exists, copying from .jar...");
			saveResource("config.json", false);

			if (enablingPlugin) {
				logger.info("");
				logger.info("  Welcome to Multipacks API for Spigot!");
				logger.info("  You are using server version " + getServer().getVersion());
				logger.info("");
				logger.info("  This plugin only contains some basic APIs for other plugins");
				logger.info("  to depends on. This plugin doesn't have ability to send Multipacks");
				logger.info("  pack to client, so you might need additional plugin to do this.");
				logger.info("");
			} else {
				logger.warning("config.json appears to be deleted after using /mp reload");
				logger.warning("If this is intentional, you can ignore this message");
			}
		}

		try {
			config = IOUtils.jsonFromFile(configFile).getAsJsonObject();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Failed to get configuration from config.json! (Permission error?)");
			return;
		}

		JsonArray repositoriesJson = Selects.getChain(config.get("repositories"), v -> v.getAsJsonArray(), null);
		String[] repositoriesStr;
		repos = new ArrayList<>();

		if (repositoriesJson == null) {
			logger.warning("'repositories' is not defined in configuration file, using default repository...");
			repositoriesStr = new String[] { "file:packsRepository" };
		} else {
			repositoriesStr = new String[repositoriesJson.size()];
			for (int i = 0; i < repositoriesJson.size(); i++) repositoriesStr[i] = repositoriesJson.get(i).getAsString();
		}

		for (String s : repositoriesStr) {
			PacksRepository repo = PacksRepository.parseRepository(getDataFolder(), s);
			if (repo == null) {
				logger.warning("Unknown repository: " + s);
				continue;
			}
			repos.add(repo);
		}
		logger.info(repos.size() + " repositories found in configuration file");

		int selectedRepoIndex = Selects.getChain(config.get("selectedRepo"), v -> v.getAsInt(), 0);
		if (selectedRepoIndex >= repos.size()) logger.info("Invalid repository #" + selectedRepoIndex + ", selecting no repository...");
		else selectedRepo = repos.get(selectedRepoIndex);

		if (config.has("pack")) {
			JsonObject packConfig = config.get("pack").getAsJsonObject();
			if (packConfig.has("enabled") && packConfig.get("enabled").getAsBoolean()) try {
				logger.info("Master pack bundling is enabled");
				String path = Selects.getChain(packConfig.get("folder"), j -> j.getAsString(), "master-pack");
				File masterPackDir = new File(getDataFolder(), path.replace('/', File.separatorChar));

				String result = Selects.getChain(packConfig.get("result"), j -> j.getAsString(), "master-pack.zip");
				File masterPackResult = new File(getDataFolder(), result.replace('/', File.separatorChar));

				if (packConfig.has("serving")) {
					JsonObject servingConfig = packConfig.get("serving").getAsJsonObject();
					if (servingConfig.has("enabled") && servingConfig.get("enabled").getAsBoolean()) {
						logger.info("Master pack serving is enabled");
						String builderName = Selects.getChain(servingConfig.get("servingType"), j -> j.getAsString(), "local");
						BiFunction<AbstractMPLogger, JsonObject, PackServer> builder = PackServer.BUILDERS.get(builderName);
						if (builder == null) {
							builder = LocalPackServer::new;
							logger.warning("Unknown pack server: " + builderName);
						}

						packServer = builder.apply(logger, servingConfig);
						logger.info("Pack server is ready!");
					}
				}

				if (masterPackDir.exists()) {
					masterPack = new Pack(masterPackDir);
					packArtifact = masterPackResult;
					rebuildMasterPack();
				} else {
					logger.error("Cannot open " + masterPackDir + ": Doesn't exists");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Rebuild master pack. Most of the time this action is performed manually using command.
	 */
	public boolean rebuildMasterPack() throws IOException {
		packOutput = null;

		if (masterPack == null) {
			logger.warning("Cannot rebuild master pack: Pack is not defined in configuration.");
			logger.warning("(reload configuration if you just recently enabled it)");
			return false;
		}

		logger.info("Building master pack...");
		PackBundler bundler = newBundler(getAvailableRepositories(), false);
		if (!new File(packArtifact, "..").exists()) new File(packArtifact, "..").mkdirs();

		try (FileOutputStream out = new FileOutputStream(packArtifact)) {
			packOutput = bundler.bundle(masterPack, out, new BundleInclude[] { BundleInclude.RESOURCES });
			for (Consumer<BundleResult> c : onMasterRebuild) c.accept(packOutput);
			logger.info("Master pack built!");

			// Optionally serving the pack
			if (packServer != null) for (Player p : getServer().getOnlinePlayers()) {
				supplyMasterPack(p);
			}

			return true;
		}
	}

	/**
	 * Ask the packs server to supply the pack to target player. Does not return anything.
	 */
	public void supplyMasterPack(Player p) {
		packServer.serve(p, packArtifact).whenComplete((result, e) -> {
			if (e != null) {
				e.printStackTrace();
				logger.warning("Cannot serve master pack to " + p.getName() + " (UUID = " + p.getUniqueId() + ")");
			}
		});
	}

	@Override
	public void onDisable() {
		selectedRepo = null;
		repos = null;
		config = null;
		logger = null;

		// Resources releasing
		for (URLClassLoader loader : MultipacksPlugin.JAR_HANDLES) {
			try {
				loader.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Cannot close " + loader.getName() + " class loader. Expect some memory leaks when using /reload");
			}
		}

		onMasterRebuild.clear();
	}

	/**
	 * Get currently selected repository.
	 */
	public PacksRepository getSelectedRepository() { return selectedRepo; }

	/**
	 * Get a list of repositories that's available. This list is modifiable: you can add your
	 * own repository if you want (think of paid repository access for example).
	 */
	public List<PacksRepository> getAvailableRepositories() { return repos; }

	/**
	 * Create new bundler, which can be used to bundle resources and data packs.
	 * @param additionalRepos Additional repository to use, besides from user's configured repos.
	 * @param ignoreResolveFail Ignore dependencies resolution errors.
	 * @param ignoreFeatures List of features to ignore while bundling.
	 */
	public PackBundler newBundler(@Nullable Collection<PacksRepository> additionalRepos, boolean ignoreResolveFail, BundleIgnore... ignoreFeatures) {
		PackBundler bundler = new PackBundler(logger);
		bundler.repositories.addAll(repos);

		bundler.ignoreResolveFail = ignoreResolveFail;
		bundler.bundlingIgnores.addAll(Arrays.asList(ignoreFeatures));
		if (additionalRepos != null) bundler.repositories.addAll(additionalRepos);

		return bundler;
	}

	/**
	 * Obtain the master pack build output. Quite useful for plugins that want to use Multipacks where player can mix
	 * a bunch of packs together. Returns null if master pack is disabled.
	 */
	public BundleResult getBuildOutput() { return packOutput; }

	/**
	 * Obtain the master pack artifact location. This is the location that you'll have to serve it to players. Returns
	 * null if master pack is disabled.
	 */
	public File getBuildArtifactLocation() { return packArtifact; }

	/**
	 * Register callback and call it when the master pack is built.
	 * @return true if the callback is registered.
	 */
	public boolean registerMasterPackBuildCallback(Consumer<BundleResult> callback) { return onMasterRebuild.add(callback); }

	/**
	 * Unregister callback, prevent it from being called when the master pack is built.
	 * @return true if the callback is unregistered.
	 */
	public boolean unregisterMasterPackBuildCallback(Consumer<BundleResult> callback) { return onMasterRebuild.remove(callback); }

	/**
	 * Get the pack server that is serving the pack every time the pack is built.
	 */
	public PackServer getPackServer() { return packServer; }

	/**
	 * Get instance of this plugin. This is the main entry point for accessing most of Multipacks features on Spigot.
	 */
	public static MultipacksSpigot getInstance() {
		return INSTANCE;
	}

	/**
	 * Convert {@link ResourcePath} to {@link NamespacedKey}. This does call the "internal use only" constructor
	 * (and they might remove it in the future).
	 */
	@SuppressWarnings("deprecation")
	public static NamespacedKey toNamespacedKey(ResourcePath resPath) {
		return new NamespacedKey(resPath.namespace, resPath.path);
	}

	/**
	 * Convert {@link NamespacedKey} to {@link ResourcePath}.
	 */
	public static ResourcePath toResourcePath(NamespacedKey key) {
		return new ResourcePath(key.getNamespace(), key.getKey());
	}
}
