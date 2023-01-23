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

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Subcommand;

public class MultipacksCommand extends Command {
	public final CLIPlatform platform;

	@Subcommand("search") public final SearchCommand search;
	@Subcommand("build") public final BuildCommand build;
	@Subcommand("install") public final InstallCommand install;

	public MultipacksCommand(CLIPlatform platform) {
		this.platform = platform;

		this.search = new SearchCommand(this);
		this.build = new BuildCommand(this);
		this.install = new InstallCommand(this);

		helpName = "java multipacks.cli.Main";
		helpDescription = "Multipacks CLI";
	}

	@Override
	protected void onExecuteWithoutSubcommand() throws CommandException {
		printHelp(System.out);
	}
}
