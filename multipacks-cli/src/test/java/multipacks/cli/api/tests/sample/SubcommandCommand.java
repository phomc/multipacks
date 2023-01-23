package multipacks.cli.api.tests.sample;

import multipacks.cli.api.Command;
import multipacks.cli.api.CommandException;
import multipacks.cli.api.annotations.Argument;
import multipacks.cli.api.annotations.Option;

public class SubcommandCommand extends Command {
	@Argument(0) public double seconds;
	@Argument(value = 1, optional = true) public String optional = "default value";

	@Option({ "-a", "--A" }) public String anotherOption = "default value";

	@Override
	protected void onExecute() throws CommandException {
		System.out.println(seconds + " seconds");
		System.out.println("optional = " + optional);
		System.out.println("anotherOption = " + anotherOption);
	}
}
