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
package multipacks.cli.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.logging.LoggingStage;
import multipacks.packs.LocalPack;
import multipacks.packs.meta.PackIdentifier;
import multipacks.repository.LocalRepository;

/**
 * @author nahkd
 *
 */
public class InstallCommand extends Command {
	public final CLIPlatform platform;

	@Argument(value = 0, helpName = "path/to/packDir")
	public String pathToPack = ".";

	public InstallCommand(MultipacksCommand parent) {
		this.platform = parent.platform;
		helpName = "install";
		helpDescription = "Install pack to local repository";
	}

	@Override
	protected void onExecute() throws CommandException {
		Path packDir = new File(pathToPack).toPath();
		if (!Files.exists(packDir)) throw new CommandException("Directory not found: " + packDir);
		if (!Files.isDirectory(packDir)) throw new CommandException("Not a directory: " + packDir);
		if (!Files.exists(packDir.resolve(LocalPack.FILE_INDEX))) throw new CommandException("Missing " + LocalPack.FILE_INDEX + ": " + packDir);

		PackIdentifier id;

		try (LoggingStage stage = platform.getLogger().newStage("Install", "Initialize")) {
			LocalRepository installRepo = platform.getInstallRepository();
			if (installRepo == null) throw new CommandException("Install repository does not defined in .multipacks/multipacks.config.json. Consider adding '\"install\": \"path/to/repo\"' to config file.");

			LocalPack pack = new LocalPack(packDir);

			try {
				pack.loadFromStorage();
			} catch (IOException e) {
				throw new CommandException("An error occured", e);
			}

			stage.newStage("Install pack");
			id = installRepo.upload(pack).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new CommandException("An error occured", e);
		}

		System.out.println("Pack is installed to local repository: " + id.name + " version " + id.packVersion);
	}
}
