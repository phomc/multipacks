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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import multipacks.bundling.BundleResult;
import multipacks.bundling.Bundler;
import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;
import multipacks.logging.LoggingStage;
import multipacks.packs.LocalPack;
import multipacks.versioning.Version;

/**
 * @author nahkd
 *
 */
public class BuildCommand extends Command {
	public final CLIPlatform platform;

	@Argument(value = 0, optional = true, helpName = "path/to/packDir = .")
	public String pathToPack = ".";

	@Option(value = { "--target", "-T" }, helpDescription = "Game version to target (default to pack source game version)")
	public String targetGameVersion;

	@Option(value = { "--output", "-O" }, helpDescription = "Output file name (default is '<pack-name>-v<version>.zip')")
	public String outputDestination;

	@Option(value = "--override", helpDescription = "Override existing output file (if exists)")
	public boolean override = false;

	@Option(value = { "--watch", "-W" }, helpDescription = "Rebuild pack when changes are made (override will also be enabled)")
	public boolean watch = false;

	public BuildCommand(MultipacksCommand parent) {
		this.platform = parent.platform;
		helpName = "build";
		helpDescription = "Build pack";
	}

	@Override
	protected void onExecute() throws CommandException {
		Path packDir = new File(pathToPack).toPath();
		if (!Files.exists(packDir)) throw new CommandException("Directory not found: " + packDir);
		if (!Files.isDirectory(packDir)) throw new CommandException("Not a directory: " + packDir);
		if (!Files.exists(packDir.resolve(LocalPack.FILE_INDEX))) throw new CommandException("Missing " + LocalPack.FILE_INDEX + ": " + packDir);

		System.out.println("Building '" + packDir + "' ...");
		if (watch) System.out.println("Watch mode is enabled!");
		buildOnce(packDir, watch || override);

		if (watch) {
			platform.getLogger().info("Getting files watcher ready...");

			try (LoggingStage stage = platform.getLogger().newStage("Watch", "Initialize watch service")) {
				WatchService service = packDir.getFileSystem().newWatchService();
				WatchKey key = packDir.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

				stage.newStage("Initalize stopper");
				AtomicBoolean stopping = new AtomicBoolean(false);
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						platform.getLogger().info("Shutting down watch service...");
						stopping.set(true);
					}
				});

				stage.newStage("Watching");
				AtomicBoolean built = new AtomicBoolean(true);
				while (!stopping.get()) {
					List<WatchEvent<?>> event = key.pollEvents();

					if (event.size() == 0) {
						if (built.get()) {
							Thread.sleep(100);
							continue;
						}

						System.out.println("Changes finalized, building...");
						buildOnce(packDir, true);
						built.set(true);
					} else {
						built.set(false);
					}
				}
			} catch (IOException | InterruptedException e) {
				throw new CommandException("An error occured while initalizing watch", e);
			}
		}
	}

	private void buildOnce(Path packDir, boolean override) {
		try (LoggingStage stage = platform.getLogger().newStage("Build", "Initialize", 3)) {
			LocalPack pack = new LocalPack(packDir);

			try {
				pack.loadFromStorage();
			} catch (IOException e) {
				throw new CommandException("An error occured", e);
			}

			File outputFile = new File(outputDestination != null? outputDestination : (pack.getIndex().name + "-v" + pack.getIndex().packVersion.toStringNoPrefix() + ".zip"));
			if (outputFile.exists()) {
				if (!override) throw new CommandException("File is already exists: " + outputFile + ". Override that file with --override=true option.");
				platform.getLogger().warning("File is already exists: {}. Deleting... (--override=true option or watch mode)", outputFile);
				outputFile.delete();
			}

			stage.newStage("VFS Build");
			Bundler bundler = new Bundler().fromPlatform(platform);
			Version targetGameVersion = this.targetGameVersion != null? new Version(this.targetGameVersion) : pack.getIndex().sourceGameVersion;
			platform.getLogger().info("Building " + pack.getIndex().name + " version " + pack.getIndex().packVersion + " (Target game version " + targetGameVersion + ")...");
			BundleResult result = bundler.bundle(pack, targetGameVersion);

			stage.newStage("Writing Zip file");
			try (FileOutputStream stream = new FileOutputStream(outputFile)) {
				result.writeZipData(stream);
			} catch (IOException e) {
				throw new RuntimeException("Failed to write to " + outputFile, e);
			}
		}
	}
}
