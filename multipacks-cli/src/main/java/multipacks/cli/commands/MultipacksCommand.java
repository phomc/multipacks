package multipacks.cli.commands;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Subcommand;

public class MultipacksCommand extends Command {
	public final CLIPlatform platform;

	@Subcommand("search")
	public final SearchCommand search;

	public MultipacksCommand(CLIPlatform platform) {
		this.platform = platform;
		this.search = new SearchCommand(this);

		helpName = "java multipacks.cli.Main";
		helpDescription = "Multipacks CLI";
	}

	@Override
	protected void onExecuteWithoutSubcommand() throws CommandException {
		printHelp(System.out);
	}
}
