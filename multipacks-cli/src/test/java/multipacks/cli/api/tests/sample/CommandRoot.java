package multipacks.cli.api.tests.sample;

import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;
import multipacks.cli.api.annotations.Subcommand;

public class CommandRoot extends Command {
	@Argument(0) public String firstArg;
	@Argument(1) public int secondArg;

	@Option("--my-option") public String myOption;

	@Subcommand("subcommand") public final SubcommandCommand subcommand = new SubcommandCommand();

	@Override
	protected void onExecute() throws CommandException {
		System.out.println("firstArg = " + firstArg);
		System.out.println("secondArg = " + secondArg);
		System.out.println("myOption = " + myOption);
	}
}
