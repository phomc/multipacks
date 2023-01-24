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

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import multipacks.plugins.InternalSystemPlugin;
import multipacks.spigot.platform.SpigotPlatform;
import multipacks.utils.Constants;
import multipacks.utils.PlatformAPI;
import multipacks.utils.ResourcePath;

@PlatformAPI
public class MultipacksSpigot extends JavaPlugin {
	private static SpigotPlatform platform;

	@Override
	public void onEnable() {
		platform = new SpigotPlatform(this);
		platform.getLogger().info("Multipacks for Spigot, version {} (API version {})", getDescription().getVersion(), getDescription().getAPIVersion());
		platform.getLogger().info("Server software version: {}", Bukkit.getVersion());

		platform.loadPlugin(new ResourcePath(Constants.SYSTEM_NAMESPACE, "builtin/internal_system_plugin"), new InternalSystemPlugin());

		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			platform.getLogger().info("Finalizing Multipacks plugins registry...");
			platform.finalizePluginsLoad();
		});
	}

	public void reloadPlugin() {
		try {
			platform.reloadConfig();
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
}
