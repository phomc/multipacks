package multipacks.cli.commands;

import multipacks.cli.CLIPlatform;
import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;

public class MultipacksCommand extends Command {
	private CLIPlatform platform;

	public MultipacksCommand(CLIPlatform platform) {
		this.platform = platform;
		helpName = "java multipacks.cli.Main";
		helpDescription = "Multipacks CLI";
	}

	@Override
	protected void onExecute() throws CommandException {
		printHelp(System.out);
	}
}
