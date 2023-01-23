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
