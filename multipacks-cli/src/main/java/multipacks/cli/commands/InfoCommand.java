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
import multipacks.cli.api.console.TableDisplay;
import multipacks.packs.LocalPack;
import multipacks.packs.meta.PackIndex;
import multipacks.repository.query.PackQuery;

/**
 * @author nahkd
 *
 */
public class InfoCommand extends Command {
	public final CLIPlatform platform;

	@Argument(value = 0, optional = true, helpName = "path/to/packDir = .")
	public String pathToPack = ".";

	public InfoCommand(MultipacksCommand parent) {
		this.platform = parent.platform;
		helpName = "info";
		helpDescription = "View pack index info";
	}

	@Override
	protected void onExecute() throws CommandException {
		Path packDir = new File(pathToPack).toPath();
		if (!Files.exists(packDir)) throw new CommandException("Directory not found: " + packDir);
		if (!Files.isDirectory(packDir)) throw new CommandException("Not a directory: " + packDir);
		if (!Files.exists(packDir.resolve(LocalPack.FILE_INDEX))) throw new CommandException("Missing " + LocalPack.FILE_INDEX + ": " + packDir);

		LocalPack pack = new LocalPack(packDir);

		try {
			pack.loadFromStorage();
		} catch (IOException e) {
			throw new CommandException("An error occured", e);
		}

		PackIndex index = pack.getIndex();
		TableDisplay table = new TableDisplay();

		System.out.println("Viewing info for " + index.name + " version " + index.packVersion + ":");
		table.add("  General ", null);
		table.add("    Pack name ", index.name);
		table.add("    Pack version ", index.packVersion.toStringNoPrefix());
		table.add("    Author ", index.author);
		table.add("    Description ", index.description);

		table.add("  Generation ", null);
		table.add("    Source game version ", index.sourceGameVersion.toStringNoPrefix());
		table.add("    pack.mcmeta preview ", index.buildPackMcmeta(index.sourceGameVersion).toString());

		table.add("  Dependencies ", "(" + index.dependencies.size() + ")");
		for (PackQuery query : index.dependencies) table.add(null, query.toString());

		table.add("  Features ", "(" + index.features.size() + ")");
		for (String feature : index.features) table.add("    " + feature, getFeatureDescription(feature));
		table.print(System.out);
	}

	private String getFeatureDescription(String feature) {
		return switch (feature) {
		case "update_1_20" -> "Minecraft 1.20 update preview";
		case "bundle" -> "Bundle feature preview";
		default -> null;
		};
	}
}
