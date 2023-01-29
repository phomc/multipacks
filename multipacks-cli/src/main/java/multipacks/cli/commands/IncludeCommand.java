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

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.packs.LocalPack;
import multipacks.vfs.Vfs;

/**
 * @author nahkd
 *
 */
public class IncludeCommand extends Command {
	public final CLIPlatform platform;

	@Argument(value = 0, helpName = "assets/path/to/file.ext")
	public String filePath;

	@Argument(value = 1, optional = true, helpName = "path/to/packDir = .")
	public String pathToPack = ".";

	public IncludeCommand(MultipacksCommand parent) {
		this.platform = parent.platform;
		helpName = "include";
		helpDescription = "Include file from game installation to pack folder";
	}

	@Override
	protected void onExecute() throws CommandException {
		Path packDir = new File(pathToPack).toPath();
		if (!Files.exists(packDir)) throw new CommandException("Directory not found: " + packDir);
		if (!Files.isDirectory(packDir)) throw new CommandException("Not a directory: " + packDir);
		if (!Files.exists(packDir.resolve(LocalPack.FILE_INDEX))) throw new CommandException("Missing " + LocalPack.FILE_INDEX + ": " + packDir);

		Vfs temp = Vfs.createVirtualRoot();
		platform.getGameJarFile(temp, new multipacks.vfs.Path(filePath));

		try {
			temp.dumpContentTo(packDir);
		} catch (IOException e) {
			throw new CommandException("An error occured while writing to pack folder", e);
		}
	}
}
