package multipacks.cli.commands;

import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Option;

public class MultipacksCommand extends Command {
	@Option(value = "-myOpt", helpDescription = "Cool description")
	public String myOption;

	@Override
	protected void onExecute() throws CommandException {
		printHelp(System.out);
	}
}
