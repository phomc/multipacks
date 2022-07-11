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
package multipacks.spigot.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import multipacks.management.PacksRepository;
import multipacks.management.PacksUploadable;
import multipacks.packs.Pack;
import multipacks.plugins.MultipacksPlugin;
import multipacks.spigot.MultipacksSpigot;
import multipacks.utils.Selects;

public class MainCommand implements TabCompleter, CommandExecutor {
	private static final String RELOAD = "multipacks.admin.reload";
	private static final String REBUILD = "multipacks.admin.rebuild";
	private static final String INSTALL = "multipacks.admin.install";

	private MultipacksSpigot plugin;

	public MainCommand(MultipacksSpigot plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			print(sender, "&cMultipacks &7by &fMangoPlex");
			print(sender, "&7(&f" + plugin.getAvailableRepositories().size() + " &7repositories, &f" + MultipacksPlugin.PLUGINS.size() + " &7plugins)");
			printCommand(sender, RELOAD, label + " reload", "Reload Multipacks Spigot configuration");
			printCommand(sender, REBUILD, label + " rebuild", "Rebuild master pack (must be enabled in config)");
			printCommand(sender, INSTALL, label + " install &epath/to/packdir", "Install pack to selected repository");
			return true;
		}
		if (args[0].equalsIgnoreCase("reload") && sender.hasPermission(RELOAD)) {
			print(sender, "&7Reloading Multipacks...");
			plugin.reloadJsonConfig(false);
			return print(sender, "&7Reloaded!");
		}
		if (args[0].equalsIgnoreCase("rebuild") && sender.hasPermission(REBUILD)) {
			print(sender, "&7Rebuilding master pack...");
			boolean success;
			try {
				success = plugin.rebuildMasterPack();
			} catch (Exception e) {
				e.printStackTrace();
				return print(sender, "&cCommand failed: &f" + Selects.firstNonNull(e.getMessage(), "Details printed in console"));
			}

			return print(sender, success? "&7Master pack rebuilt!" : "&cRebuild failed: &fDetail messages printed in console");
		}
		if (args[0].equalsIgnoreCase("install") && sender.hasPermission(INSTALL)) {
			if (args.length < 2) return print(sender, "&cCommand failed: &fMissing path to pack");
			PacksRepository repo = plugin.getSelectedRepository();
			if (repo == null) return print(sender, "&cCommand failed: &fNo repository selected");
			if (!(repo instanceof PacksUploadable uploadable)) return print(sender, "&cCommand failed: &fSelected repository does not have uploading mechanism");

			print(sender, "&7Installing...");
			plugin.getLogger().info("Reading pack folder (relative to current working directory)...");
			File packRoot = new File(args[1]);
			if (!packRoot.exists() || !packRoot.isDirectory()) {
				plugin.getLogger().severe("Cannot install " + args[1] + ": Doesn't exists or not a directory");
				return print(sender, "&cCommand failed: &f" + args[1] + " doesn't exists or not a folder");
			}

			boolean success;
			try {
				Pack pack = new Pack(packRoot);
				success = uploadable.putPack(pack);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return print(sender, "&cCommand failed: &f" + Selects.firstNonNull(e.getMessage(), "Access is denied while installing"));
			} catch (Exception e) {
				e.printStackTrace();
				return print(sender, "&cCommand failed: &f" + Selects.firstNonNull(e.getMessage(), "Details printed in console"));
			}

			return print(sender, success? "&7Pack installed! Use /" + label + " rebuild to rebuild pack" : "&cCommand failed: &fInstall failed");
		}

		return print(sender, "&cUnknown command: &f/" + label + " " + String.join(" ", args));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		ArrayList<String> suggestions = new ArrayList<>();
		String partial = args[args.length - 1];
		if (args.length == 1) {
			suggest(sender, RELOAD, "reload", suggestions);
			suggest(sender, REBUILD, "rebuild", suggestions);
			suggest(sender, INSTALL, "install", suggestions);
			return suggestions.stream().filter(v -> v.startsWith(partial)).toList();
		}

		return suggestions;
	}

	private boolean print(CommandSender sender, String... message) {
		for (String m : message) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
		return true;
	}

	private boolean printCommand(CommandSender sender, String permission, String cmd, String description) {
		if (!sender.hasPermission(permission)) return true;
		return print(sender, "&7/&f" + cmd + " &8: &7" + description);
	}

	private void suggest(CommandSender sender, String permission, String cmd, List<String> suggestions) {
		if (sender.hasPermission(permission)) suggestions.add(cmd);
	}
}
