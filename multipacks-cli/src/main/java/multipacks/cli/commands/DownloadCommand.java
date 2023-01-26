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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;
import multipacks.logging.LoggingStage;
import multipacks.packs.Pack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.repository.Repository;
import multipacks.versioning.Version;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class DownloadCommand extends Command {
	public final CLIPlatform platform;

	@Argument(value = 0, helpName = "packName")
	public String packName;

	@Argument(value = 1, helpName = "packVersion")
	public String packVersion;

	@Argument(value = 2, optional = true, helpName = "path/to/destination")
	public String pathToDestination;

	@Option(value = "--override", helpDescription = "Override existing output file (if exists)")
	public boolean override = false;

	public DownloadCommand(MultipacksCommand parent) {
		this.platform = parent.platform;
		helpName = "download";
		helpDescription = "Download the specified pack from repositories";
	}

	@Override
	protected void onExecute() throws CommandException {
		PackIdentifier id = new PackIdentifier(packName, new Version(packVersion));
		Path destDir = new File(pathToDestination != null? pathToDestination : (packName + "-v" + packVersion)).toPath();
		if (Files.exists(destDir)) {
			if (!override) if (!override) throw new CommandException("File is already exists: " + destDir + ". Override that file with --override=true option.");
			platform.getLogger().warning("File is already exists: {}. Deleting... (--override=true option)", destDir);

			try (Stream<Path> walk = Files.walk(destDir)) {
				walk.sorted(Comparator.reverseOrder()).map(p -> p.toFile()).forEach(f -> f.delete());
			} catch (IOException e) {
				throw new CommandException("An error occured while deleting " + destDir, e);
			}
		}

		try {
			Files.createDirectories(destDir);
		} catch (IOException e) {
			throw new CommandException("Failed to create " + destDir, e);
		}

		System.out.println("Downloading " + packName + " version " + packVersion + "...");

		try (LoggingStage stage = platform.getLogger().newStage("Download", "Initialize")) {
			boolean isDownloaded = false;

			for (Repository repo : platform.getRepositories()) {
				platform.getLogger().info("Trying repository '{}'...", repo);

				Pack pack = repo.obtain(id).get();
				if (pack == null) continue;

				stage.newStage("Download pack");
				try {
					Vfs content = pack.createVfs();
					content.dumpContentTo(destDir);
					isDownloaded = true;
				} catch (IOException e) {
					throw new CommandException("An error occured while copying content from pack to " + destDir, e);
				}

				break;
			}

			if (!isDownloaded) throw new CommandException("Pack is not downloaded (pack does not exists in repositories?)");
		} catch (InterruptedException | ExecutionException e) {
			throw new CommandException("An error occured", e);
		}

		System.out.println("Download completed!");
	}
}
