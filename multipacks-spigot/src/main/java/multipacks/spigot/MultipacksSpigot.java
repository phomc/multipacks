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
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import multipacks.bundling.BundleIgnore;
import multipacks.bundling.PackBundler;
import multipacks.management.PacksRepository;
import multipacks.utils.IOUtils;
import multipacks.utils.Selects;

/**
 * The entry point for accessing Multipacks API for Spigot.
 * @author nahkd
 * @see #getInstance()
 *
 */
public class MultipacksSpigot extends JavaPlugin {
	private JavaMPLogger logger;
	private JsonObject config;
	private List<PacksRepository> repos;
	private PacksRepository selectedRepo;

	private static MultipacksSpigot INSTANCE;

	@Override
	public void onEnable() {
		INSTANCE = this;
		long benchmarkStart = System.nanoTime();

		logger = new JavaMPLogger(getLogger());

		File configFile = new File(getDataFolder(), "config.json");

		if (!configFile.exists()) {
			logger.warning("config.json doesn't exists, copying from .jar...");
			saveResource("config.json", false);

			logger.info("");
			logger.info("  Welcome to Multipacks API for Spigot!");
			logger.info("  You are using server version " + getServer().getVersion());
			logger.info("");
			logger.info("  This plugin only contains some basic APIs for other plugins");
			logger.info("  to depends on. This plugin doesn't have ability to send Multipacks");
			logger.info("  pack to client, so you might need additional plugin to do this.");
			logger.info("");
		}

		try {
			config = IOUtils.jsonFromFile(configFile).getAsJsonObject();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Failed to get configuration from config.json! (Permission error?)");
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

		logger.info("Plugin enabled in " + new DecimalFormat("#0.000").format(((System.nanoTime() - benchmarkStart) / 1e6)) + "ms");
	}

	@Override
	public void onDisable() {
		selectedRepo = null;
		repos = null;
		config = null;
		logger = null;
	}

	/**
	 * Get currently selected repository.
	 */
	public PacksRepository getSelectedRepository() {
		return selectedRepo;
	}

	/**
	 * Get a list of repositories that's available. This list is modifiable: you can add your
	 * own repository if you want (think of paid repository access for example).
	 */
	public List<PacksRepository> getAvailableRepositories() {
		return repos;
	}

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
	 * Get instance of this plugin.
	 */
	public static MultipacksSpigot getInstance() {
		return INSTANCE;
	}
}
