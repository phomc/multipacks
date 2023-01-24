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

import java.util.concurrent.ExecutionException;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.logging.LoggingStage;
import multipacks.packs.meta.PackIdentifier;
import multipacks.repository.LocalRepository;
import multipacks.versioning.Version;

/**
 * @author nahkd
 *
 */
public class UninstallCommand extends Command {
	public final CLIPlatform platform;

	@Argument(value = 0, helpName = "packName")
	public String packName;

	@Argument(value = 1, helpName = "packVersion")
	public String packVersion;

	public UninstallCommand(MultipacksCommand parent) {
		this.platform = parent.platform;
		helpName = "uninstall";
		helpDescription = "Uninstall pack from local repository";
	}

	@Override
	protected void onExecute() throws CommandException {
		PackIdentifier id = new PackIdentifier(packName, new Version(packVersion));

		try (LoggingStage stage = platform.getLogger().newStage("Uninstall", "Initialize")) {
			LocalRepository installRepo = platform.getInstallRepository();
			if (installRepo == null) throw new CommandException("Install repository does not defined in .multipacks/multipacks.config.json. Consider adding '\"install\": \"path/to/repo\"' to config file.");

			stage.newStage("Uninstall pack");
			installRepo.delete(id).get();
		} catch (ExecutionException | InterruptedException e) {
			throw new CommandException("An error occured", e);
		}
	}
}
