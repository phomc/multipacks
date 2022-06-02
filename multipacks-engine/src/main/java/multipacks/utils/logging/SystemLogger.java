package multipacks.utils.logging;

import java.io.PrintStream;
import java.util.stream.Stream;

public class SystemLogger extends AbstractMPLogger {
	private void log(PrintStream printer, Object... objs) {
		printer.println(String.join("", Stream.of(objs).map(v -> v.toString()).toArray(String[]::new)));
	}

	@Override
	public void info(Object... objs) {
		log(System.out, objs);
	}

	@Override
	public void warning(Object... objs) {
		log(System.err, objs);
	}

	@Override
	public void error(Object... objs) {
		log(System.err, objs);
	}

	@Override
	public void critical(Object... objs) {
		log(System.err, objs);
	}
}
