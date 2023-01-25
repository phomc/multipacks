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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import multipacks.plugins.InternalSystemPlugin;
import multipacks.spigot.platform.SpigotPlatform;
import multipacks.utils.Constants;
import multipacks.utils.PlatformAPI;
import multipacks.utils.ResourcePath;
import multipacks.versioning.Version;

@PlatformAPI
public class MultipacksSpigot extends JavaPlugin {
	private static final Pattern VERSION_PATTERN = Pattern.compile("MC:\\s+?(?<version>((\\d+)\\.?)+)");
	private static SpigotPlatform platform;

	@Override
	public void onEnable() {
		platform = new SpigotPlatform(this);
		platform.getLogger().info("Multipacks for Spigot, version {} (API version {})", getDescription().getVersion(), getDescription().getAPIVersion());
		platform.getLogger().info("Server software version: {}", Bukkit.getVersion());

		Version gameVersion = detectGameVersion();
		if (gameVersion == null) {
			platform.getLogger().warning("---");
			platform.getLogger().warning("Warning: Cannot obtain game version (Bukkit.getVersion() = '{}')", Bukkit.getVersion());
			platform.getLogger().warning("Please send the message above to our issues tracker: {}", Constants.URL_ISSUES_TRACKER);
			platform.getLogger().warning("Multipacks for Spigot will failback target game version to {}", getDescription().getAPIVersion());
			platform.getLogger().warning("---");
		} else {
			platform.getLogger().info("Detected game version: {}", gameVersion);
		}

		platform.addPlugin(new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/internal_system_plugin"), new InternalSystemPlugin());

		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			reloadPlugin();
		});
	}

	public void reloadPlugin() {
		try {
			platform.loadConfig();
		} catch (Exception e) {
			throw new RuntimeException("Failed to reload plugin", e);
		}
	}

	@Override
	public void onDisable() {
		platform = null;
	}

	public static SpigotPlatform getPlatform() {
		if (platform == null) throw new NullPointerException("Plugin is not enabled yet");
		return platform;
	}

	public static Version detectGameVersion() {
		String bukkitVersion = Bukkit.getVersion();
		Matcher matcher = VERSION_PATTERN.matcher(bukkitVersion);

		if (matcher.find()) {
			return new Version(matcher.group("version"));
		}

		return null;
	}
}
