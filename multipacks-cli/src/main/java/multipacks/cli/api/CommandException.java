package multipacks.cli.api;

import java.io.Serial;

public class CommandException extends RuntimeException {
	@Serial private static final long serialVersionUID = 6573680395025609741L;

	public CommandException(String message) {
		super(message);
	}

	public CommandException(String message, Throwable e) {
		super(message, e);
	}
}
