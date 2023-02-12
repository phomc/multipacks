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

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonObject;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;
import multipacks.logging.LoggingStage;
import multipacks.packs.LocalPack;
import multipacks.packs.meta.PackIndex;
import multipacks.utils.io.IOUtils;
import multipacks.versioning.Version;

/**
 * @author nahkd
 *
 */
public class InitCommand extends Command {
	public final CLIPlatform platform;

	@Argument(value = 0, optional = true, helpName = "path/to/destination")
	public String pathToDestination = ".";

	@Option(value = "--override", helpDescription = "Override existing output file (if exists)")
	public boolean override = false;

	public InitCommand(MultipacksCommand parent) {
		this.platform = parent.platform;
		helpName = "init";
		helpDescription = "Initialize a brand new Multipacks pack";
	}

	@Override
	protected void onExecute() throws CommandException {
		Path destDir = new File(pathToDestination).toPath();
		Path destIndex = destDir.resolve(LocalPack.FILE_INDEX);

		if (Files.notExists(destDir)) {
			try {
				Files.createDirectories(destDir);
			} catch (IOException e) {
				throw new CommandException("Failed to create folders for " + destDir, e);
			}
		}

		if (Files.exists(destIndex)) {
			if (!override) if (!override) throw new CommandException("File is already exists: " + destIndex + ". Override that file with --override=true option.");
			platform.getLogger().warning("File is already exists: {}. Deleting... (--override=true option)", destIndex);

			try {
				Files.delete(destIndex);
			} catch (IOException e) {
				throw new CommandException("Failed to delete " + destIndex, e);
			}
		}

		Console console = System.console();
		if (console == null) throw new CommandException("Failed to open console to read CLI input. Please create '" + LocalPack.FILE_INDEX + "' manually.");

		System.out.println(LocalPack.FILE_INDEX + " will be created in " + destDir.toAbsolutePath());
		System.out.println("You are about to create a new Multipacks pack folder");
		System.out.println("Please enter information about your pack below");
		System.out.println();

		String packName = console.readLine("Pack name (Allowed characters: [A-Za-z0-9\\-_]): ");
		String author = getOrDefault(console.readLine("Author (optional): "), null);
		String description = getOrDefault(console.readLine("Description (optional): "), null);
		Version packVersion = new Version(getOrDefault(console.readLine("Pack version (default = 0.0.1): "), "0.0.1"));
		Version gameVersion = new Version(getOrDefault(console.readLine("Targeted game version (default = 1.19.3): "), "1.19.3"));

		try (LoggingStage stage = platform.getLogger().newStage("Init", "Multipacks Index file", 1)) {
			PackIndex index = new PackIndex(packName, packVersion, author, gameVersion, description);
			JsonObject json = index.toJson();

			try {
				IOUtils.jsonToStream(json, Files.newOutputStream(destIndex));
			} catch (IOException e) {
				throw new CommandException("Failed to create " + destIndex, e);
			}
		}
	}

	private String getOrDefault(String src, String def) {
		if (src == null || src.trim().length() == 0) return def;
		return src;
	}
}
