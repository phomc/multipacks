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

import java.util.concurrent.CompletableFuture;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.packs.meta.PackIdentifier;
import multipacks.repository.query.PackQuery;

public class SearchCommand extends Command {
	public final CLIPlatform platform;

	@Argument(value = 0, helpName = "Query")
	public String query;

	public SearchCommand(MultipacksCommand parent) {
		this.platform = parent.platform;
		helpName = "search";
		helpDescription = "Search across all repositories for packs";
	}

	@Override
	protected void onExecute() throws CommandException {
		PackQuery query = PackQuery.parse(this.query);
		System.out.println("Searching... (query = " + query + ")");

		CompletableFuture.allOf(platform.getRepositories().stream().map(repo -> {
			return repo.search(query).thenAccept(collection -> {
				System.out.println("  From repository: " + repo);

				for (PackIdentifier id : collection) {
					System.out.println("    " + id.name + " version " + id.packVersion);
				}
			});
		}).toArray(CompletableFuture[]::new));

		System.out.println("Search done.");
	}
}
